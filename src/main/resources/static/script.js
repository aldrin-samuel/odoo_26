let currentUserRole = "PUBLIC";
let currentPage = "dashboard";

const API = "/api/vehicles";
const DRIVER_API = "/api/drivers";
const TRIP_API = "/api/trips";
const MAINT_API = "/api/maintenance";
const FUEL_API = "/api/fuel";
const EXP_API = "/api/expenses";

// ---------------- Initialization ----------------
window.onload = async function () {
    try {
        const res = await fetch("/api/session/user");
        if (!res.ok) throw new Error("Session API failed");

        const session = await res.json();
        if (session.role === "PUBLIC" || !session.role) {
            window.location.href = "/login";
            return;
        }

        currentUserRole = session.role;
        document.getElementById("user-info").innerHTML = `<strong>${session.name}</strong><br>${session.role.replace("_", " ")}`;

        buildSidebar();
        navigateTo("dashboard");

    } catch (e) {
        console.error(e);
        window.location.href = "/login";
    }
};

// ---------------- Sidebar & Navigation ----------------
function buildSidebar() {
    const menu = document.getElementById("nav-menu");
    let items = [
        { id: "dashboard", icon: "📊", label: "Dashboard", roles: ["ALL"] },
        { id: "vehicles", icon: "🚛", label: "Vehicles", roles: ["FLEET_MANAGER", "SAFETY_OFFICER", "FINANCIAL_ANALYST"] },
        { id: "drivers", icon: "🧑‍✈️", label: "Drivers", roles: ["FLEET_MANAGER", "SAFETY_OFFICER"] },
        { id: "trips", icon: "🛣️", label: "Trips", roles: ["FLEET_MANAGER", "DRIVER", "SAFETY_OFFICER"] },
        { id: "maintenance", icon: "🔧", label: "Maintenance", roles: ["FLEET_MANAGER"] },
        { id: "fuel", icon: "⛽", label: "Fuel Logs", roles: ["FLEET_MANAGER", "FINANCIAL_ANALYST"] },
        { id: "expenses", icon: "💰", label: "Expenses", roles: ["FLEET_MANAGER", "FINANCIAL_ANALYST"] },
        { id: "analytics", icon: "📈", label: "Analytics", roles: ["FLEET_MANAGER", "FINANCIAL_ANALYST"] }
    ];

    menu.innerHTML = items.filter(i => i.roles.includes("ALL") || i.roles.includes(currentUserRole))
        .map(i => `<li class="nav-item" onclick="navigateTo('${i.id}', this)">${i.icon} ${i.label}</li>`)
        .join('');
}

function navigateTo(page, clickedElement) {
    currentPage = page;
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    if (clickedElement) {
        clickedElement.classList.add('active');
    } else {
        const items = document.querySelectorAll('.nav-item');
        if (items.length > 0) items[0].classList.add('active');
    }

    const titles = { dashboard:"Dashboard", vehicles:"Vehicle Registry", drivers:"Driver Management", trips:"Trip Management", maintenance:"Maintenance", fuel:"Fuel Logs", expenses:"Expenses", analytics:"Fleet Analytics" };
    document.getElementById("page-title").innerText = titles[page];

    const actionBtn = document.getElementById("topbar-action-btn");
    actionBtn.style.display = "none";
    actionBtn.innerText = "+ Add New";

    const content = document.getElementById("app-content");
    content.innerHTML = '<div style="text-align:center; padding:50px; color:#666;">Loading...</div>';

    switch(page) {
        case 'dashboard': loadDashboard(); break;
        case 'vehicles': loadVehicles(); break;
        case 'drivers': loadDrivers(); break;
        case 'trips': loadTrips(); break;
        case 'maintenance': loadMaintenance(); break;
        case 'fuel': loadFuel(); break;
        case 'expenses': loadExpenses(); break;
        case 'analytics': loadAnalytics(); break;
    }
}

// ---------------- Generic Helpers & Validation ----------------
async function apiCall(url, options = {}) {
    const res = await fetch(url, { headers: { 'Content-Type': 'application/json' }, ...options });
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || "API Error");
    }
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

function openModal(title, bodyHtml, saveCallback) {
    document.getElementById("modal-title").innerText = title;
    document.getElementById("modal-body").innerHTML = bodyHtml;
    document.getElementById("modal-overlay").classList.add("active");

    const btn = document.getElementById("modal-save-btn");
    const newBtn = btn.cloneNode(true);
    btn.parentNode.replaceChild(newBtn, btn);
    newBtn.id = "modal-save-btn";
    newBtn.innerText = "Save";

    // WRAP SAVE CALLBACK WITH VALIDATION
    newBtn.onclick = () => {
        if (validateModalForm()) {
            saveCallback();
        }
    };
}

function closeModal() {
    document.getElementById("modal-overlay").classList.remove("active");
}

// --- DYNAMIC VALIDATION ENGINE ---
function validateModalForm() {
    const inputs = document.querySelectorAll('#modal-body input[required], #modal-body select[required]');
    let isValid = true;
    let firstInvalid = null;

    // Clear previous error styles
    document.querySelectorAll('#modal-body .form-control').forEach(el => {
        el.style.borderColor = '';
    });

    inputs.forEach(input => {
        const val = input.value.trim();
        if (!val) {
            input.style.borderColor = '#ef4444'; // Red border
            isValid = false;
            if (!firstInvalid) firstInvalid = input;
        } else {
            input.style.borderColor = '#10b981'; // Green border if valid
        }
    });

    // Specific Number Validations (prevent negatives)
    const numberInputs = document.querySelectorAll('#modal-body input[type="number"]');
    numberInputs.forEach(input => {
        if (input.value && parseFloat(input.value) < 0) {
            input.style.borderColor = '#ef4444';
            isValid = false;
            if (!firstInvalid) firstInvalid = input;
        }
    });

    // Specific Dropdown Validations (ensure not the default placeholder)
    const selects = document.querySelectorAll('#modal-body select');
    selects.forEach(select => {
        if (select.value === "" && select.hasAttribute('required')) {
            select.style.borderColor = '#ef4444';
            isValid = false;
            if (!firstInvalid) firstInvalid = select;
        } else if (select.value !== "") {
            select.style.borderColor = '#10b981';
        }
    });

    if (!isValid) {
        alert("Please fix the highlighted errors (missing or invalid fields).");
        if (firstInvalid) firstInvalid.focus();
        return false;
    }
    return true;
}

// Helper to generate required inputs quickly
const req = 'required';
const num = 'type="number" step="any" min="0"';

// ---------------- Pages ----------------

// --- DASHBOARD ---
async function loadDashboard() {
    const d = await apiCall("/api/dashboard");
    let notificationsHtml = '';
    if (d.notifications && d.notifications.length > 0) {
        notificationsHtml = `<div class="notification-box"><h3>🔔 Alerts & Notifications</h3><ul>${d.notifications.map(n => `<li>${n}</li>`).join('')}</ul></div>`;
    }
    let tripsHtml = '';
    if (d.activeTripsList && d.activeTripsList.length > 0) {
        tripsHtml = `<div class="table-container" style="margin-top: 25px;"><table class="data-table"><thead><tr><th>Trip #</th><th>Vehicle</th><th>Driver</th><th>Route</th></tr></thead><tbody>${d.activeTripsList.map(t => `<tr><td><strong>${t.tripNumber}</strong></td><td>${t.vehicleReg}</td><td>${t.driverName}</td><td>${t.route}</td></tr>`).join('')}</tbody></table></div>`;
    } else {
        tripsHtml = `<div style="text-align:center; padding:40px; color:#94a3b8; margin-top:25px; background:white; border-radius:12px;">🛣️ No active trips currently on the road.</div>`;
    }
    document.getElementById("app-content").innerHTML = `${notificationsHtml}<div class="kpi-grid">
        <div class="kpi-card" style="border-color:var(--primary)"><h3>Available Vehicles</h3><div class="value">${d.availableVehicles}</div></div>
        <div class="kpi-card" style="border-color:var(--success)"><h3>Active Trips</h3><div class="value">${d.activeTrips}</div></div>
        <div class="kpi-card" style="border-color:var(--warning)"><h3>In Maintenance</h3><div class="value">${d.vehiclesInMaintenance}</div></div>
        <div class="kpi-card" style="border-color:purple"><h3>Fleet Utilization</h3><div class="value">${d.fleetUtilization}%</div></div>
    </div>${tripsHtml}`;
}

// --- VEHICLES ---
async function loadVehicles() {
    if(currentUserRole === "FLEET_MANAGER") { const btn = document.getElementById("topbar-action-btn"); btn.style.display = "block"; btn.onclick = () => openVehicleModal(); }
    const data = await apiCall(API);
    document.getElementById("app-content").innerHTML = `<div class="table-container"><table class="data-table">
        <thead><tr><th>Reg No</th><th>Name</th><th>Type</th><th>Capacity</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>${data.map(v => {
            let act = '';
            if (currentUserRole !== "FINANCIAL_ANALYST") act += `<button class="btn btn-warning btn-sm" onclick="retireVehicle(${v.id})">Retire</button> `;
            if (currentUserRole === "FLEET_MANAGER") act += `<button class="btn btn-danger btn-sm" onclick="deleteVehicle(${v.id})">Delete</button>`;
            return `<tr><td>${v.registrationNumber}</td><td>${v.vehicleName}</td><td>${v.vehicleType}</td><td>${v.maxLoadCapacity} kg</td><td>${v.status}</td><td class="actions">${act}</td></tr>`;
        }).join('')}</tbody></table></div>`;
}

function openVehicleModal() {
    openModal("Add Vehicle", `
        <div class="form-group"><label>Registration Number *</label><input class="form-control" id="f-reg" ${req} placeholder="e.g., MH-12-AB-1234"></div>
        <div class="form-group"><label>Vehicle Name *</label><input class="form-control" id="f-vname" ${req} placeholder="e.g., Tata Ace"></div>
        <div class="form-group"><label>Vehicle Type *</label><input class="form-control" id="f-vtype" ${req} placeholder="e.g., Truck, Van"></div>
        <div class="form-group"><label>Max Capacity (kg) *</label><input class="form-control" id="f-cap" ${num} ${req} placeholder="e.g., 1000"></div>
        <div class="form-group"><label>Acquisition Cost (₹) *</label><input class="form-control" id="f-cost" ${num} ${req} placeholder="e.g., 500000"></div>
    `, async () => {
        await apiCall(API, { method: "POST", body: JSON.stringify({ registrationNumber: document.getElementById("f-reg").value, vehicleName: document.getElementById("f-vname").value, vehicleType: document.getElementById("f-vtype").value, maxLoadCapacity: parseFloat(document.getElementById("f-cap").value), acquisitionCost: parseFloat(document.getElementById("f-cost").value) }) });
        closeModal(); loadVehicles();
    });
}

async function deleteVehicle(id) { if(confirm("Delete this vehicle?")) { await apiCall(API+"/"+id, {method:"DELETE"}); loadVehicles(); } }
async function retireVehicle(id) { if(confirm("Retire this vehicle?")) { await apiCall(API+"/"+id+"/retire", {method:"PUT"}); loadVehicles(); } }

// --- DRIVERS ---
// --- DRIVERS ---
async function loadDrivers() {
    if(currentUserRole === "FLEET_MANAGER" || currentUserRole === "SAFETY_OFFICER") {
        const btn = document.getElementById("topbar-action-btn");
        btn.style.display = "block";
        btn.onclick = () => openDriverModal();
    }

    const data = await apiCall(DRIVER_API);
    document.getElementById("app-content").innerHTML = `<div class="table-container"><table class="data-table">
        <thead><tr><th>Name</th><th>License</th><th>Expiry</th><th>Contact</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>${data.map(d => {
            let act = '';

            // Context-aware action buttons based on current status
            if (d.status === "SUSPENDED") {
                act = `<button class="btn btn-success btn-sm" onclick="reactivateDriver(${d.id})">Reactivate</button>
                       <button class="btn btn-primary btn-sm" onclick="openLicenseModal(${d.id}, '${d.name}')">Update License</button>`;
            } else if (d.status === "OFF_DUTY") {
                act = `<button class="btn btn-success btn-sm" onclick="setAvailable(${d.id})">Set Available</button>
                       <button class="btn btn-primary btn-sm" onclick="openLicenseModal(${d.id}, '${d.name}')">Update License</button>`;
            } else if (currentUserRole === "SAFETY_OFFICER" || currentUserRole === "FLEET_MANAGER") {
                // If AVAILABLE or ON_TRIP (though ON_TRIP shouldn't really show actions, this is safe)
                act = `<button class="btn btn-primary btn-sm" onclick="openLicenseModal(${d.id}, '${d.name}')">Update License</button>
                       <button class="btn btn-warning btn-sm" onclick="suspendDriver(${d.id})">Suspend</button>
                       <button class="btn btn-secondary btn-sm" onclick="setOffDuty(${d.id})">Off Duty</button>`;
            }

            // Fleet managers can delete
            if (currentUserRole === "FLEET_MANAGER") {
                act += ` <button class="btn btn-danger btn-sm" onclick="deleteDriver(${d.id})">Delete</button>`;
            }

            return `<tr><td>${d.name}</td><td>${d.licenseNumber}</td><td>${d.licenseExpiry}</td><td>${d.contactNumber}</td><td>${d.status}</td><td class="actions">${act}</td></tr>`;
        }).join('')}</tbody></table></div>`;
}

function openDriverModal() { /* ... keep existing openDriverModal function ... */ }
function openLicenseModal(id, name) { /* ... keep existing openLicenseModal function ... */ }

async function suspendDriver(id) { if(confirm("Suspend this driver?")) { await apiCall(DRIVER_API+"/"+id+"/suspend", {method:"PUT"}); loadDrivers(); } }
async function reactivateDriver(id) { if(confirm("Reactivate this driver?")) { await apiCall(DRIVER_API+"/"+id+"/reactivate", {method:"PUT"}); loadDrivers(); } }
async function deleteDriver(id) { if(confirm("Delete this driver?")) { await apiCall(DRIVER_API+"/"+id, {method:"DELETE"}); loadDrivers(); } }

// --- ADD THESE TWO NEW FUNCTIONS ---
async function setOffDuty(id) {
    if(confirm("Set this driver as Off Duty?")) {
        await apiCall(DRIVER_API+"/"+id+"/off-duty", {method:"PUT"});
        loadDrivers();
    }
}

async function setAvailable(id) {
    if(confirm("Set this driver back to Available?")) {
        await apiCall(DRIVER_API+"/"+id+"/available", {method:"PUT"});
        loadDrivers();
    }
}

function openDriverModal() {
    openModal("Add Driver", `
        <div class="form-group"><label>Full Name *</label><input class="form-control" id="f-dname" ${req} placeholder="e.g., John Doe"></div>
        <div class="form-group"><label>Personal Email *</label><input type="email" class="form-control" id="f-demail" ${req} placeholder="e.g., john@gmail.com"></div>
        <div class="form-group"><label>License Number *</label><input class="form-control" id="f-dlic" ${req} placeholder="e.g., MH-1234567890"></div>
        <div class="form-group"><label>Category *</label><input class="form-control" id="f-dcat" ${req} placeholder="e.g., Heavy, Light"></div>
        <div class="form-group"><label>Expiry Date *</label><input type="date" class="form-control" id="f-dexp" ${req}></div>
        <div class="form-group"><label>Contact Number *</label><input class="form-control" id="f-dphone" ${req} placeholder="e.g., 9876543210"></div>
    `, async () => {
        const res = await apiCall(DRIVER_API, {
            method: "POST",
            body: JSON.stringify({
                name: document.getElementById("f-dname").value,
                email: document.getElementById("f-demail").value, // ADDED THIS
                licenseNumber: document.getElementById("f-dlic").value,
                licenseCategory: document.getElementById("f-dcat").value,
                licenseExpiry: document.getElementById("f-dexp").value,
                contactNumber: document.getElementById("f-dphone").value
            })
        });
        closeModal();
        alert(`Driver Created!\n\nLogin Email: ${res.generatedEmail}\nPassword: ${res.generatedPassword}`);
        loadDrivers();
    });
}

function openLicenseModal(id, name) {
    openModal(`Update License: ${name}`, `<div class="form-group"><label>New Expiry Date *</label><input type="date" class="form-control" id="f-newexp" ${req}></div>`, async () => {
        await apiCall(DRIVER_API+"/"+id+"/license", { method: "PUT", body: JSON.stringify({ newExpiryDate: document.getElementById("f-newexp").value }) });
        closeModal(); loadDrivers();
    });
}

async function suspendDriver(id) { if(confirm("Suspend this driver?")) { await apiCall(DRIVER_API+"/"+id+"/suspend", {method:"PUT"}); loadDrivers(); } }
async function reactivateDriver(id) { if(confirm("Reactivate this driver?")) { await apiCall(DRIVER_API+"/"+id+"/reactivate", {method:"PUT"}); loadDrivers(); } }
async function deleteDriver(id) { if(confirm("Delete this driver?")) { await apiCall(DRIVER_API+"/"+id, {method:"DELETE"}); loadDrivers(); } }

// --- TRIPS ---
async function loadTrips() {
    if(currentUserRole === "DRIVER" || currentUserRole === "FLEET_MANAGER") { const btn = document.getElementById("topbar-action-btn"); btn.style.display = "block"; btn.innerText = "+ Dispatch Trip"; btn.onclick = () => openTripModal(); }
    const data = await apiCall(TRIP_API);
    document.getElementById("app-content").innerHTML = `<div class="table-container"><table class="data-table">
        <thead><tr><th>Trip #</th><th>Vehicle</th><th>Driver</th><th>Route</th><th>Cargo</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>${data.map(t => {
            let act = '';
            if (t.status === "DISPATCHED" && (currentUserRole === "DRIVER" || currentUserRole === "FLEET_MANAGER")) {
                act = `<button class="btn btn-success btn-sm" onclick="openCompleteModal(${t.id})">Complete</button> <button class="btn btn-danger btn-sm" onclick="cancelTrip(${t.id})">Cancel</button>`;
            }
            return `<tr><td>${t.tripNumber || t.id}</td><td>${t.vehicleRegNumber}</td><td>${t.driverName}</td><td>${t.source} ➜ ${t.destination}</td><td>${t.cargoWeight} kg</td><td>${t.status}</td><td class="actions">${act}</td></tr>`;
        }).join('')}</tbody></table></div>`;
}

async function openTripModal() {
    const vehicles = await apiCall(API); const drivers = await apiCall(DRIVER_API);
    const vOpts = vehicles.filter(v=>v.status==="AVAILABLE").map(v=>`<option value="${v.id}">${v.registrationNumber}</option>`).join('');
    const dOpts = drivers.filter(d=>d.status==="AVAILABLE").map(d=>`<option value="${d.id}">${d.name}</option>`).join('');

    openModal("Dispatch Trip", `
        <div class="form-group"><label>Vehicle *</label><select class="form-control" id="f-tveh" ${req}><option value="">-- Select Vehicle --</option>${vOpts}</select></div>
        <div class="form-group"><label>Driver *</label><select class="form-control" id="f-tdrv" ${req}><option value="">-- Select Driver --</option>${dOpts}</select></div>
        <div class="form-group"><label>Source *</label><input class="form-control" id="f-src" ${req} placeholder="e.g., Mumbai"></div>
        <div class="form-group"><label>Destination *</label><input class="form-control" id="f-dest" ${req} placeholder="e.g., Pune"></div>
        <div class="form-group"><label>Cargo Weight (kg) *</label><input class="form-control" id="f-cargo" ${num} ${req} placeholder="e.g., 500"></div>
        <div class="form-group"><label>Planned Distance (km) *</label><input class="form-control" id="f-dist" ${num} ${req} placeholder="e.g., 150"></div>
    `, async () => {
        await apiCall(TRIP_API, { method: "POST", body: JSON.stringify({ vehicle: {id: document.getElementById("f-tveh").value}, driver: {id: document.getElementById("f-tdrv").value}, source: document.getElementById("f-src").value, destination: document.getElementById("f-dest").value, cargoWeight: parseFloat(document.getElementById("f-cargo").value), plannedDistance: parseFloat(document.getElementById("f-dist").value) }) });
        closeModal(); loadTrips();
    });
}

function openCompleteModal(id) {
    openModal("Complete Trip #" + id, `
        <div class="form-group"><label>Actual Distance (km) *</label><input class="form-control" id="f-adist" ${num} ${req} placeholder="e.g., 160"></div>
        <div class="form-group"><label>Fuel Consumed (L) *</label><input class="form-control" id="f-fuel" ${num} ${req} step="0.1" placeholder="e.g., 20.5"></div>
        <div class="form-group"><label>End Odometer *</label><input class="form-control" id="f-odom" ${num} ${req} placeholder="e.g., 10500"></div>
    `, async () => {
        await apiCall(TRIP_API+"/"+id+"/complete", { method: "PUT", body: JSON.stringify({ actualDistance: parseFloat(document.getElementById("f-adist").value), fuelConsumed: parseFloat(document.getElementById("f-fuel").value), endOdometer: parseFloat(document.getElementById("f-odom").value) }) });
        closeModal(); loadTrips();
    });
}

async function cancelTrip(id) { if(confirm("Cancel Trip?")) { await apiCall(TRIP_API+"/"+id+"/cancel", {method:"PUT"}); loadTrips(); } }

// --- MAINTENANCE ---
async function loadMaintenance() {
    if(currentUserRole === "FLEET_MANAGER") { const btn = document.getElementById("topbar-action-btn"); btn.style.display = "block"; btn.onclick = () => openMaintModal(); }
    const data = await apiCall(MAINT_API);
    document.getElementById("app-content").innerHTML = `<div class="table-container"><table class="data-table">
        <thead><tr><th>Vehicle</th><th>Type</th><th>Desc</th><th>Cost</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>${data.map(m => `<tr><td>${m.registrationNumber}</td><td>${m.type}</td><td>${m.description}</td><td>₹${m.cost}</td><td>${m.status}</td><td class="actions">${m.status==="ACTIVE" ? `<button class="btn btn-success btn-sm" onclick="completeMaint(${m.id})">Done</button>` : ''}</td></tr>`).join('')}</tbody></table></div>`;
}

async function openMaintModal() {
    const vehicles = await apiCall(API);
    const vOpts = vehicles.map(v=>`<option value="${v.id}">${v.registrationNumber}</option>`).join('');
    openModal("Add Maintenance", `
        <div class="form-group"><label>Vehicle *</label><select class="form-control" id="f-mveh" ${req}>${vOpts}</select></div>
        <div class="form-group"><label>Type *</label><input class="form-control" id="f-mtype" ${req} placeholder="e.g., Oil Change"></div>
        <div class="form-group"><label>Description *</label><input class="form-control" id="f-mdesc" ${req} placeholder="Details of work done"></div>
        <div class="form-group"><label>Cost (₹) *</label><input class="form-control" id="f-mcost" ${num} ${req} placeholder="e.g., 2500"></div>
    `, async () => {
        await apiCall(MAINT_API, { method: "POST", body: JSON.stringify({ vehicleId: parseInt(document.getElementById("f-mveh").value), type: document.getElementById("f-mtype").value, description: document.getElementById("f-mdesc").value, cost: parseFloat(document.getElementById("f-mcost").value) }) });
        closeModal(); loadMaintenance();
    });
}

async function completeMaint(id) { await apiCall(MAINT_API+"/"+id+"/complete", {method:"PUT"}); loadMaintenance(); }

// --- FUEL ---
async function loadFuel() {
    if(currentUserRole === "FLEET_MANAGER" || currentUserRole === "FINANCIAL_ANALYST") { const btn = document.getElementById("topbar-action-btn"); btn.style.display = "block"; btn.onclick = () => openFuelModal(); }
    const data = await apiCall(FUEL_API);
    document.getElementById("app-content").innerHTML = `<div class="table-container"><table class="data-table">
        <thead><tr><th>Date</th><th>Vehicle</th><th>Driver</th><th>Liters</th><th>Total Cost</th></tr></thead>
        <tbody>${data.map(f => `<tr><td>${f.date}</td><td>${f.registrationNumber}</td><td>${f.driverName || 'N/A'}</td><td>${f.liters}L</td><td>₹${f.totalCost}</td></tr>`).join('')}</tbody></table></div>`;
}

async function openFuelModal() {
    const vehicles = await apiCall(API);
    const vOpts = vehicles.map(v=>`<option value="${v.id}">${v.registrationNumber}</option>`).join('');
    openModal("Add Fuel Log", `
        <div class="form-group"><label>Vehicle *</label><select class="form-control" id="f-fveh" ${req}>${vOpts}</select></div>
        <div class="form-group"><label>Liters *</label><input class="form-control" id="f-flit" type="number" step="0.1" min="0.1" ${req} placeholder="e.g., 40.5"></div>
        <div class="form-group"><label>Cost/Liter (₹) *</label><input class="form-control" id="f-fcpl" type="number" step="0.1" min="0.1" ${req} placeholder="e.g., 105.50"></div>
        <div class="form-group"><label>Date *</label><input type="date" class="form-control" id="f-fdate" ${req}></div>
    `, async () => {
        await apiCall(FUEL_API, { method: "POST", body: JSON.stringify({ vehicleId: parseInt(document.getElementById("f-fveh").value), liters: parseFloat(document.getElementById("f-flit").value), costPerLiter: parseFloat(document.getElementById("f-fcpl").value), date: document.getElementById("f-fdate").value }) });
        closeModal(); loadFuel();
    });
}

// --- EXPENSES ---
async function loadExpenses() {
    if(currentUserRole === "FLEET_MANAGER") { const btn = document.getElementById("topbar-action-btn"); btn.style.display = "block"; btn.onclick = () => openExpModal(); }
    const data = await apiCall(EXP_API);
    document.getElementById("app-content").innerHTML = `<div class="table-container"><table class="data-table">
        <thead><tr><th>Date</th><th>Vehicle</th><th>Driver</th><th>Type</th><th>Description</th><th>Amount</th></tr></thead>
        <tbody>${data.map(e => `<tr><td>${e.date}</td><td>${e.vehicleName || 'N/A'}</td><td>${e.driverName || 'N/A'}</td><td>${e.type}</td><td>${e.description}</td><td>₹${e.amount}</td></tr>`).join('')}</tbody></table></div>`;
}

async function openExpModal() {
    const vehicles = await apiCall(API);
    const vOpts = `<option value="">-- None --</option>` + vehicles.map(v=>`<option value="${v.id}">${v.registrationNumber}</option>`).join('');
    openModal("Add Expense", `
        <div class="form-group"><label>Vehicle</label><select class="form-control" id="f-eveh">${vOpts}</select></div>
        <div class="form-group"><label>Type *</label><select class="form-control" id="f-etype" ${req}><option value="">-- Select Type --</option><option value="TOLL">Toll</option><option value="PARKING">Parking</option><option value="FINE">Fine</option><option value="OTHER">Other</option></select></div>
        <div class="form-group"><label>Description *</label><input class="form-control" id="f-edesc" ${req} placeholder="e.g., Highway toll plaza"></div>
        <div class="form-group"><label>Amount (₹) *</label><input class="form-control" id="f-eamt" ${num} ${req} placeholder="e.g., 200"></div>
        <div class="form-group"><label>Date *</label><input type="date" class="form-control" id="f-edate" ${req}></div>
    `, async () => {
        const vid = document.getElementById("f-eveh").value;
        await apiCall(EXP_API, { method: "POST", body: JSON.stringify({ vehicleId: vid ? parseInt(vid) : null, type: document.getElementById("f-etype").value, description: document.getElementById("f-edesc").value, amount: parseFloat(document.getElementById("f-eamt").value), date: document.getElementById("f-edate").value }) });
        closeModal(); loadExpenses();
    });
}

// --- ANALYTICS ---
async function loadAnalytics() {
    const data = await apiCall("/api/analytics/fleet");
    document.getElementById("app-content").innerHTML = `
        <div style="margin-bottom:20px;"><button class="btn btn-primary" onclick="window.location.href='/api/analytics/fleet/csv'">⬇ Export CSV</button></div>
        <div class="table-container"><table class="data-table">
        <thead><tr><th>Vehicle</th><th>Fuel Cost</th><th>Maint Cost</th><th>Op. Cost</th><th>Fuel Eff. (km/L)</th></tr></thead>
        <tbody>${data.map(a => `<tr><td>${a.vehicleName}</td><td>₹${a.totalFuelCost}</td><td>₹${a.totalMaintenanceCost}</td><td><strong>₹${a.totalOperationalCost}</strong></td><td>${a.fuelEfficiency || 'N/A'}</td></tr>`).join('')}</tbody></table></div>`;
}