const API = "/api/vehicles";

window.onload = function () {
    loadVehicles();
    loadDashboard();
};

function loadDashboard() {

    fetch("/api/dashboard")
        .then(res => res.json())
        .then(data => {

            document.getElementById("vehicleCount").innerText =
                data.totalVehicles;

        });

}

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

        registrationNumber:
            document.getElementById("regNo").value,

        vehicleName:
            document.getElementById("vehicleName").value,

        vehicleType:
            document.getElementById("vehicleType").value,

        maxLoadCapacity:
            parseFloat(document.getElementById("capacity").value),

        status:"AVAILABLE"

    };

    fetch(API, {

        method:"POST",

        headers:{
            "Content-Type":"application/json"
        },

        body:JSON.stringify(vehicle)

    })

    .then(() => {

        loadVehicles();

        loadDashboard();

        document.getElementById("regNo").value="";
        document.getElementById("vehicleName").value="";
        document.getElementById("vehicleType").value="";
        document.getElementById("capacity").value="";

    });

}

function deleteVehicle(id){

    fetch(API+"/"+id,{
        method:"DELETE"
    })

    .then(()=>{

        loadVehicles();

        loadDashboard();

    });

}