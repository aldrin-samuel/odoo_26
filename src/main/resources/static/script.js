const API = "/api/vehicles";
const DRIVER_API = "/api/drivers";
const TRIP_API="/api/trips";

window.onload = function () {
    loadVehicles();
    loadDrivers();
    loadDashboard();
    loadTrips();
    loadTripDropdowns();
};

// ---------------- Dashboard ----------------

function loadDashboard() {
    fetch("/api/dashboard")
        .then(res => res.json())
        .then(data => {
            document.getElementById("vehicleCount").innerText = data.totalVehicles;
        });
}

// ---------------- Vehicle ----------------

function loadVehicles() {

    fetch(API)
        .then(res => res.json())
        .then(data => {

            let rows = "";

            data.forEach(vehicle => {

                rows += `
                    <tr>
                        <td>${vehicle.id}</td>
                        <td>${vehicle.registrationNumber}</td>
                        <td>${vehicle.vehicleName}</td>
                        <td>${vehicle.vehicleType}</td>
                        <td>${vehicle.status}</td>
                        <td>
                            <button onclick="deleteVehicle(${vehicle.id})">
                                Delete
                            </button>
                        </td>
                    </tr>
                `;

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
        status: "AVAILABLE"

    };

    fetch(API, {

        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(vehicle)

    })

    .then(() => {

        loadVehicles();
        loadDashboard();

    });

}

function deleteVehicle(id) {

    fetch(API + "/" + id, {

        method: "DELETE"

    })

    .then(() => {

        loadVehicles();
        loadDashboard();

    });

}

// ---------------- Driver ----------------

function loadDrivers() {

    fetch(DRIVER_API)
        .then(res => res.json())
        .then(data => {

            let rows = "";

            data.forEach(driver => {

                rows += `
                    <tr>
                        <td>${driver.id}</td>
                        <td>${driver.name}</td>
                        <td>${driver.licenseNumber}</td>
                        <td>${driver.status}</td>
                        <td>
                            <button onclick="deleteDriver(${driver.id})">
                                Delete
                            </button>
                        </td>
                    </tr>
                `;

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
        contactNumber: document.getElementById("contactNumber").value,
        status: "AVAILABLE"

    };

    fetch(DRIVER_API, {

        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(driver)

    })

    .then(() => {

        loadDrivers();
        loadDashboard();

    });

}

function deleteDriver(id) {

    fetch(DRIVER_API + "/" + id, {

        method: "DELETE"

    })

    .then(() => {

        loadDrivers();
        loadDashboard();

    });

}

function loadTripDropdowns(){

    fetch(API)
    .then(r=>r.json())
    .then(data=>{

        let options="";

        data.forEach(v=>{

            if(v.status==="AVAILABLE"){

                options+=`<option value="${v.id}">
                ${v.registrationNumber}
                </option>`;

            }

        });

        document.getElementById("tripVehicle").innerHTML=options;

    });

    fetch(DRIVER_API)
    .then(r=>r.json())
    .then(data=>{

        let options="";

        data.forEach(d=>{

            if(d.status==="AVAILABLE"){

                options+=`<option value="${d.id}">
                ${d.name}
                </option>`;

            }

        });

        document.getElementById("tripDriver").innerHTML=options;

    });

}

function createTrip(){

    const trip={

        vehicle:{
            id:parseInt(document.getElementById("tripVehicle").value)
        },

        driver:{
            id:parseInt(document.getElementById("tripDriver").value)
        },

        source:document.getElementById("tripSource").value,

        destination:document.getElementById("tripDestination").value,

        cargo:document.getElementById("tripCargo").value,

        distance:parseFloat(document.getElementById("tripDistance").value),

        revenue:parseFloat(document.getElementById("tripRevenue").value)

    };

    fetch(TRIP_API,{

        method:"POST",

        headers:{
            "Content-Type":"application/json"
        },

        body:JSON.stringify(trip)

    })

    .then(()=>{

        loadTrips();
        loadVehicles();
        loadDrivers();
        loadTripDropdowns();
        loadDashboard();

    });

}
function loadTrips(){

    fetch(TRIP_API)
    .then(r=>r.json())
    .then(data=>{

        let rows="";

        data.forEach(t=>{

            rows+=`

            <tr>

            <td>${t.id}</td>

            <td>${t.vehicle.registrationNumber}</td>

            <td>${t.driver.name}</td>

            <td>${t.source} ➜ ${t.destination}</td>

            <td>${t.status}</td>

            <td>${t.revenue}</td>

            <td>

            <button onclick="completeTrip(${t.id})">

            Complete

            </button>

            </td>

            </tr>

            `;

        });

        document.getElementById("tripTable").innerHTML=rows;

    });

}
function completeTrip(id){

    fetch(TRIP_API+"/"+id+"/complete",{

        method:"PUT"

    })

    .then(()=>{

        loadTrips();
        loadVehicles();
        loadDrivers();
        loadTripDropdowns();
        loadDashboard();

    });

}