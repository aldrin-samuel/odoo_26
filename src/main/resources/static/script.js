const API = "/api/vehicles";
const DRIVER_API = "/api/drivers";
const TRIP_API = "/api/trips";
const MAINT_API = "/api/maintenance";
const FUEL_API = "/api/fuel";
const EXP_API = "/api/expenses";

let currentCompleteTripId = null;
let currentUserRole = "PUBLIC";

// ---------------- RBAC & Session Check ----------------
window.onload = async function () {
    try {
        const res = await fetch("/api/session/user");
        const session = await res.json();
        currentUserRole = session.role;

        if (currentUserRole === "PUBLIC") {
            window.location.href = "/login";
            return;
        }

        // Update UI with user info
        if (document.getElementById("welcomeUser")) {
            document.getElementById("welcomeUser").innerText = "Welcome, " + session.name + " (" + currentUserRole + ")";
        }

        applyRBAC(); // Hide/Show sections based on role

        // Load Data
        loadDashboard();
        loadVehicles();
        loadDrivers();
        loadTrips();
        loadMaintenance();
        loadFuelLogs();
        loadExpenses();
        loadAnalytics();
        loadVehicleDropdowns();

    } catch (e) {
        window.location.href = "/login";
    }
};

function applyRBAC() {
    const fleetSections = document.querySelectorAll(".rbac-fleet");
    const driverSections = document.querySelectorAll(".rbac-driver");
    const safetySections = document.querySelectorAll(".rbac-safety");
    const financeSections = document.querySelectorAll(".rbac-finance");

    // Hide all role-specific sections first
    fleetSections.forEach(el => el.style.display = "none");
    driverSections.forEach(el => el.style.display = "none");
    safetySections.forEach(el => el.style.display = "none");
    financeSections.forEach(el => el.style.display = "none");

    // Show based on role
    if (currentUserRole === "FLEET_MANAGER") {
        fleetSections.forEach(el => el.style.display = "");
        driverSections.forEach(el => el.style.display = "");
        safetySections.forEach(el => el.style.display = "");
        financeSections.forEach(el => el.style.display = "");
    }
    else if (currentUserRole === "DRIVER") {
        driverSections.forEach(el => el.style.display = "");
        document.querySelectorAll(".rbac-readonly-driver").forEach(el => el.style.display = ""); // Can see vehicles/drivers for dropdowns
    }
    else if (currentUserRole === "SAFETY_OFFICER") {
        safetySections.forEach(el => el.style.display = "");
        document.querySelectorAll(".rbac-readonly-safety").forEach(el => el.style.display = ""); // Read-only vehicles/trips
    }
    else if (currentUserRole === "FINANCIAL_ANALYST") {
        financeSections.forEach(el => el.style.display = "");
        document.querySelectorAll(".rbac-readonly-finance").forEach(el => el.style.display = ""); // Read-only vehicles
    }
}

// ---------------- Dashboard ----------------
function loadDashboard() {
    fetch("/api/dashboard")
        .then(res => res.json())
        .then(data => {
            document.getElementById("availableVehicles").innerText = data.availableVehicles;
            document.getElementById("activeTrips").innerText = data.activeTrips;
            document.getElementById("vehiclesInShop").innerText = data.vehiclesInMaintenance;
            document.getElementById("fleetUtilization").innerText = data.fleetUtilization + "%";
        });
}

// ---------------- Vehicle ----------------
function loadVehicles() {
    fetch(API).then(res => res.json()).then(data => {
        let rows = "";
        data.forEach(v => {
            rows += `<tr>
                <td>${v.id}</td><td>${v.registrationNumber}</td><td>${v.vehicleName}</td>
                <td>${v.vehicleType}</td><td>${v.maxLoadCapacity}</td>
                <td>${v.status}</td>
                <td><button onclick="deleteVehicle(${v.id})" style="background:#ef4444; color:white;">Delete</button></td>
            </tr>`;
        });
        document.getElementById("vehicleTable").innerHTML = rows;
    });
}

function addVehicle() {
    const vehicle = {
        registrationNumber: document.getElementById("regNo").value,
        vehicleName: document.getElementById("vehicleName").value,
        vehicleType: document.getElementById("vehicleType").value,
        maxLoadCapacity: parseFloat(document.getElementById("capacity").value),
        acquisitionCost: parseFloat(document.getElementById("acquisitionCost").value) || 0.0
    };
    fetch(API, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(vehicle) })
        .then(() => { loadVehicles(); loadDashboard(); loadVehicleDropdowns(); });
}

function deleteVehicle(id) {
    if(confirm("Delete this vehicle?")) {
        fetch(API + "/" + id, { method: "DELETE" }).then(() => { loadVehicles(); loadDashboard(); loadVehicleDropdowns(); });
    }
}

// ---------------- Driver ----------------
function loadDrivers() {
    fetch(DRIVER_API).then(res => res.json()).then(data => {
        let rows = "";
        data.forEach(d => {
            rows += `<tr>
                <td>${d.id}</td><td>${d.name}</td><td>${d.licenseNumber}</td>
                <td>${d.licenseExpiry}</td><td>${d.status}</td>
                <td><button onclick="deleteDriver(${d.id})" style="background:#ef4444; color:white;">Delete</button></td>
            </tr>`;
        });
        document.getElementById("driverTable").innerHTML = rows;
    });
}

function addDriver() {
    const driver = {
        name: document.getElementById("driverName").value,
        licenseNumber: document.getElementById("licenseNumber").value,
        licenseCategory: document.getElementById("licenseCategory").value,
        licenseExpiry: document.getElementById("licenseExpiry").value,
        contactNumber: document.getElementById("contactNumber").value
    };
    fetch(DRIVER_API, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(driver) })
        .then(() => { loadDrivers(); loadDashboard(); loadVehicleDropdowns(); });
}

function deleteDriver(id) {
    if(confirm("Delete this driver?")) {
        fetch(DRIVER_API + "/" + id, { method: "DELETE" }).then(() => { loadDrivers(); loadDashboard(); loadVehicleDropdowns(); });
    }
}

// ---------------- Dropdowns ----------------
function loadVehicleDropdowns() {
    fetch(API).then(r => r.json()).then(data => {
        let vOptions = "<option value=''>-- Select --</option>";
        let mOptions = "<option value=''>-- Select --</option>";
        let eOptions = "<option value=''>-- None --</option>";

        data.forEach(v => {
            const opt = `<option value="${v.id}">${v.registrationNumber} (${v.vehicleName})</option>`;
            vOptions += opt;
            mOptions += opt;
            eOptions += opt;
        });

        if(document.getElementById("tripVehicle")) document.getElementById("tripVehicle").innerHTML = vOptions;
        if(document.getElementById("maintVehicle")) document.getElementById("maintVehicle").innerHTML = mOptions;
        if(document.getElementById("fuelVehicle")) document.getElementById("fuelVehicle").innerHTML = mOptions;
        if(document.getElementById("expVehicle")) document.getElementById("expVehicle").innerHTML = eOptions;
    });

    fetch(DRIVER_API).then(r => r.json()).then(data => {
        let dOptions = "<option value=''>-- Select --</option>";
        data.forEach(d => { dOptions += `<option value="${d.id}">${d.name}</option>`; });
        if(document.getElementById("tripDriver")) document.getElementById("tripDriver").innerHTML = dOptions;
    });
}

// ---------------- Trips ----------------
function createTrip() {
    const trip = {
        vehicle: { id: parseInt(document.getElementById("tripVehicle").value) },
        driver: { id: parseInt(document.getElementById("tripDriver").value) },
        source: document.getElementById("tripSource").value,
        destination: document.getElementById("tripDestination").value,
        cargoWeight: parseFloat(document.getElementById("tripCargo").value),
        plannedDistance: parseFloat(document.getElementById("tripDistance").value)
    };

    fetch(TRIP_API, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(trip) })
        .then(res => {
            if (!res.ok) { return res.text().then(text => { throw new Error(text); }); }
            return res.json();
        })
        .then(() => {
            loadTrips(); loadVehicles(); loadDrivers(); loadDashboard(); loadVehicleDropdowns();
            document.getElementById("tripSource").value = "";
            document.getElementById("tripDestination").value = "";
            document.getElementById("tripCargo").value = "";
            document.getElementById("tripDistance").value = "";
        })
        .catch(err => alert("Error: " + err.message));
}

function loadTrips() {
    fetch(TRIP_API)
        .then(res => res.json())
        .then(data => {
            let rows = "";
            if (Array.isArray(data)) {
                data.forEach(t => {
                    let actions = "";
                    if (t.status === "DISPATCHED") {
                        actions = `<button onclick="showCompleteForm(${t.id})" style="background:#10b981; color:white; padding:5px; border:none; border-radius:4px; cursor:pointer;">Complete</button>
                                   <button onclick="cancelTrip(${t.id})" style="background:#ef4444; color:white; padding:5px; border:none; border-radius:4px; cursor:pointer; margin-left:5px;">Cancel</button>`;
                    }

                    rows += `<tr>
                        <td>${t.id}</td>
                        <td>${t.vehicleRegNumber}</td>
                        <td>${t.driverName}</td>
                        <td>${t.source} ➜ ${t.destination}</td>
                        <td>${t.cargoWeight}</td>
                        <td>${t.status}</td>
                        <td>${actions}</td>
                    </tr>`;
                });
            }
            document.getElementById("tripTable").innerHTML = rows || "<tr><td colspan='7' style='text-align:center;'>No trips dispatched yet</td></tr>";
        })
        .catch(error => {
            console.error(error);
            document.getElementById("tripTable").innerHTML = "<tr><td colspan='7' style='color:red; text-align:center;'>Error loading trips</td></tr>";
        });
}

function showCompleteForm(id) {
    currentCompleteTripId = id;
    document.getElementById("ctTripId").innerText = id;
    document.getElementById("completeTripCard").style.display = "block";
    document.getElementById("completeTripCard").scrollIntoView({ behavior: 'smooth' });
}

function cancelCompleteForm() {
    document.getElementById("completeTripCard").style.display = "none";
    currentCompleteTripId = null;
}

function submitCompleteTrip() {
    const body = {
        actualDistance: parseFloat(document.getElementById("ctActualDistance").value),
        fuelConsumed: parseFloat(document.getElementById("ctFuelConsumed").value),
        endOdometer: parseFloat(document.getElementById("ctEndOdometer").value)
    };

    fetch(TRIP_API + "/" + currentCompleteTripId + "/complete", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    }).then(res => {
        if (!res.ok) return res.text().then(text => { throw new Error(text); });
        cancelCompleteForm();
        loadTrips(); loadVehicles(); loadDrivers(); loadDashboard(); loadVehicleDropdowns();
    }).catch(err => alert("Completion Error: " + err.message));
}

function cancelTrip(id) {
    if (confirm("Cancel this trip? Vehicle and Driver will become available again.")) {
        fetch(TRIP_API + "/" + id + "/cancel", { method: "PUT" })
            .then(() => { loadTrips(); loadVehicles(); loadDrivers(); loadDashboard(); loadVehicleDropdowns(); });
    }
}

// ---------------- Maintenance ----------------
function loadMaintenance() {
    fetch(MAINT_API).then(r => r.json()).then(data => {
        let rows = "";
        data.forEach(m => {
            let action = m.status === "ACTIVE" ?
                `<button onclick="completeMaint(${m.id})" style="background:#10b981; color:white; padding:5px;">Mark Done</button>` : "";
            rows += `<tr>
                <td>${m.id}</td><td>${m.registrationNumber}</td><td>${m.type}</td>
                <td>${m.description}</td><td>₹${m.cost}</td><td>${m.status}</td><td>${action}</td>
            </tr>`;
        });
        document.getElementById("maintTable").innerHTML = rows;
    });
}

function addMaintenance() {
    const body = {
        vehicleId: parseInt(document.getElementById("maintVehicle").value),
        type: document.getElementById("maintType").value,
        description: document.getElementById("maintDesc").value,
        cost: parseFloat(document.getElementById("maintCost").value)
    };
    fetch(MAINT_API, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) })
        .then(() => { loadMaintenance(); loadVehicles(); loadDashboard(); loadVehicleDropdowns(); });
}

function completeMaint(id) {
    fetch(MAINT_API + "/" + id + "/complete", { method: "PUT" })
        .then(() => { loadMaintenance(); loadVehicles(); loadDashboard(); loadVehicleDropdowns(); });
}

// ---------------- Fuel ----------------
function loadFuelLogs() {
    fetch(FUEL_API).then(r => r.json()).then(data => {
        let rows = "";
        data.forEach(f => {
            rows += `<tr>
                <td>${f.id}</td><td>${f.registrationNumber}</td><td>${f.liters} L</td>
                <td>₹${f.totalCost}</td><td>${f.date}</td>
            </tr>`;
        });
        document.getElementById("fuelTable").innerHTML = rows;
    });
}

function addFuelLog() {
    const body = {
        vehicleId: parseInt(document.getElementById("fuelVehicle").value),
        liters: parseFloat(document.getElementById("fuelLiters").value),
        costPerLiter: parseFloat(document.getElementById("fuelCostPerLiter").value),
        date: document.getElementById("fuelDate").value,
        odometerReading: parseFloat(document.getElementById("fuelOdometer").value)
    };
    fetch(FUEL_API, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) })
        .then(() => { loadFuelLogs(); });
}

// ---------------- Expenses ----------------
function loadExpenses() {
    fetch(EXP_API)
        .then(res => res.json())
        .then(data => {
            let rows = "";
            if (Array.isArray(data)) {
                data.forEach(e => {
                    rows += `<tr>
                        <td>${e.id}</td>
                        <td>${e.vehicleName}</td>
                        <td>${e.type}</td>
                        <td>${e.description}</td>
                        <td>₹${e.amount}</td>
                    </tr>`;
                });
            }
            document.getElementById("expTable").innerHTML = rows || "<tr><td colspan='5' style='text-align:center;'>No expenses added yet</td></tr>";
        })
        .catch(error => {
            console.error(error);
            document.getElementById("expTable").innerHTML = "<tr><td colspan='5' style='color:red; text-align:center;'>Error loading expenses</td></tr>";
        });
}

function addExpense() {
    const vehId = document.getElementById("expVehicle").value;
    const body = {
        vehicleId: vehId ? parseInt(vehId) : null,
        type: document.getElementById("expType").value.toUpperCase(),
        description: document.getElementById("expDesc").value,
        amount: parseFloat(document.getElementById("expAmount").value),
        date: document.getElementById("expDate").value
    };
    fetch(EXP_API, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) })
        .then(() => { loadExpenses(); });
}

// ---------------- Analytics ----------------
function loadAnalytics() {
    fetch("/api/analytics/fleet").then(r => r.json()).then(data => {
        let rows = "";
        data.forEach(a => {
            let roiColor = a.roi > 0 ? "green" : "red";
            rows += `<tr>
                <td>${a.vehicleName} (${a.registrationNumber})</td>
                <td>₹${a.totalFuelCost}</td>
                <td>₹${a.totalMaintenanceCost}</td>
                <td><strong>₹${a.totalOperationalCost}</strong></td>
                <td>${a.fuelEfficiency || 'N/A'}</td>
                <td style="color: ${roiColor}; font-weight: bold;">${a.roi}%</td>
            </tr>`;
        });
        document.getElementById("analyticsTable").innerHTML = rows;
    });
}

function exportCsv() {
    window.location.href = "/api/analytics/fleet/csv";
}