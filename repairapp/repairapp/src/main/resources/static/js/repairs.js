// Données des appareils
const deviceData = {
    SMARTPHONE: {
        Apple: ['iPhone 13', 'iPhone 12', 'iPhone SE'],
        Samsung: ['Galaxy S24', 'Galaxy S23'],
        Xiaomi: ['Redmi Note 12', 'Mi 11']
    },
    ORDINATEUR_PORTABLE: {
        Apple: ['MacBook Air', 'MacBook Pro'],
        Dell: ['XPS 13', 'Inspiron 15'],
        HP: ['Spectre x360', 'Pavilion 15']
    }
};

// Fonction pour basculer entre saisie manuelle et automatique
function toggleManualEntry(reset = false) {
    const manual = document.getElementById('manualEntrySwitch').checked;
    document.getElementById('brandSelect').classList.toggle('d-none', manual);
    document.getElementById('modelSelect').classList.toggle('d-none', manual);
    document.getElementById('manualEntryFields').classList.toggle('d-none', !manual);
    document.getElementById('brandSelect').required = !manual;
    document.getElementById('modelSelect').required = !manual;
    document.getElementById('brandManual').required = manual;
    document.getElementById('modelManual').required = manual;
    
    if (reset) {
        document.getElementById('brandManual').value = '';
        document.getElementById('modelManual').value = '';
        document.getElementById('brandSelect').value = '';
        document.getElementById('modelSelect').value = '';
    }
}

// Initialisation du modal d'ajout
function initRepairModal() {
    document.getElementById('deviceType').value = '';
    updateBrandOptions();
    toggleManualEntry(true);
}

// Mise à jour des options de marque selon le type d'appareil
function updateBrandOptions() {
    const type = document.getElementById('deviceType').value;
    const brandSelect = document.getElementById('brandSelect');
    const modelSelect = document.getElementById('modelSelect');
    
    brandSelect.innerHTML = '<option value="">Sélectionnez une marque</option>';
    modelSelect.innerHTML = '<option value="">Sélectionnez un modèle</option>';
    
    if (deviceData[type]) {
        Object.keys(deviceData[type]).forEach(brand => {
            const opt = document.createElement('option');
            opt.value = brand;
            opt.textContent = brand;
            brandSelect.appendChild(opt);
        });
    }
}

// Mise à jour des options de modèle selon la marque
function updateModelOptions() {
    const type = document.getElementById('deviceType').value;
    const brand = document.getElementById('brandSelect').value;
    const modelSelect = document.getElementById('modelSelect');
    
    modelSelect.innerHTML = '<option value="">Sélectionnez un modèle</option>';
    
    if (deviceData[type] && deviceData[type][brand]) {
        deviceData[type][brand].forEach(model => {
            const opt = document.createElement('option');
            opt.value = model;
            opt.textContent = model;
            modelSelect.appendChild(opt);
        });
    }
}

// Soumission du formulaire d'ajout
function submitRepair() {
    const form = document.getElementById('addRepairForm');
    let isValid = true;
    
    form.querySelectorAll('input, select, textarea').forEach(field => {
        if (!field.classList.contains('d-none') && field.hasAttribute('required') && !field.value.trim()) {
            isValid = false;
            field.classList.add('is-invalid');
        } else {
            field.classList.remove('is-invalid');
        }
    });
    
    if (!isValid) {
        alert('Veuillez remplir tous les champs obligatoires');
        return;
    }
    
    const submitBtn = document.querySelector('#addRepairModal .btn-primary');
    const spinner = submitBtn.querySelector('.spinner-border');
    submitBtn.disabled = true;
    if (spinner) spinner.classList.remove('d-none');
    
    let brand = document.getElementById('manualEntrySwitch').checked
        ? document.getElementById('brandManual').value
        : document.getElementById('brandSelect').value;
    
    let model = document.getElementById('manualEntrySwitch').checked
        ? document.getElementById('modelManual').value
        : document.getElementById('modelSelect').value;
    
    const data = {
        clientName: document.getElementById('clientName').value,
        deviceType: document.getElementById('deviceType').value,
        brand: brand,
        model: model,
        issueDescription: document.getElementById('issueDescription').value,
        cost: document.getElementById('cost').value,
        advance: document.getElementById('advance').value,
        urgent: document.getElementById('urgent').checked
    };
    
    fetch('/repairs', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (response.ok) {
            location.reload();
        } else {
            return response.text().then(text => {
                throw new Error(text || 'Erreur lors de l\'ajout de la réparation');
            });
        }
    })
    .catch(error => {
        alert('Erreur: ' + error.message);
    })
    .finally(() => {
        if (spinner) spinner.classList.add('d-none');
        submitBtn.disabled = false;
    });
}

// Modification d'une réparation
window.editRepair = function(id) {
    fetch(`/repairs/${id}`)
        .then(response => response.json())
        .then(repair => {
            document.getElementById('editRepairId').value = repair.id;
            const form = document.getElementById('editRepairForm');
            form.elements['clientName'].value = repair.clientName;
            form.elements['deviceType'].value = repair.deviceType;
            form.elements['brand'].value = repair.brand;
            form.elements['model'].value = repair.model;
            form.elements['issueDescription'].value = repair.issueDescription;
            form.elements['cost'].value = repair.cost;
            form.elements['advance'].value = repair.advance;
            form.elements['status'].value = repair.status;
            form.elements['clientName'].focus();
            new bootstrap.Modal(document.getElementById('editRepairModal')).show();
        });
};

// Affichage des informations de suivi
window.showTrackingInfo = function(id) {
    fetch(`/repairs/${id}`)
        .then(response => response.json())
        .then(repair => {
            document.getElementById('modalTrackingCode').textContent = repair.trackingCode;
            document.getElementById('modalClientName').textContent = repair.clientName;
            document.getElementById('modalDevice').textContent = 
                `${repair.deviceType} ${repair.brand} ${repair.model}`;
            document.getElementById('modalStatus').textContent = repair.status;
            document.getElementById('modalCost').textContent = repair.cost.toFixed(2) + ' €';
            document.getElementById('modalAdvance').textContent = repair.advance.toFixed(2) + ' €';
            document.getElementById('modalRemaining').textContent = 
                (repair.cost - repair.advance).toFixed(2) + ' €';
            
            new bootstrap.Modal(document.getElementById('trackingInfoModal')).show();
        });
};

// Mise à jour d'une réparation
window.updateRepair = function() {
    const id = document.getElementById('editRepairId').value;
    const formData = new FormData(document.getElementById('editRepairForm'));
    const data = Object.fromEntries(formData.entries());
    
    fetch(`/repairs/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (response.ok) {
            location.reload();
        } else {
            return response.text().then(text => {
                throw new Error(text || 'Erreur lors de la mise à jour');
            });
        }
    })
    .catch(error => alert('Erreur: ' + error.message));
};

// Suppression d'une réparation
window.deleteRepair = function(id) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette réparation ?')) {
        fetch(`/repairs/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                location.reload();
            } else {
                return response.text().then(text => {
                    throw new Error(text || 'Erreur lors de la suppression');
                });
            }
        })
        .catch(error => alert('Erreur: ' + error.message));
    }
};

// Initialisation des événements
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('deviceType').addEventListener('change', updateBrandOptions);
    document.getElementById('brandSelect').addEventListener('change', updateModelOptions);
    document.getElementById('manualEntrySwitch').addEventListener('change', () => toggleManualEntry(false));
    
    const modal = document.getElementById('addRepairModal');
    if (modal) {
        modal.addEventListener('show.bs.modal', initRepairModal);
    }
}); 