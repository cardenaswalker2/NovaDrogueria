document.addEventListener("DOMContentLoaded", function() {
    // 1. Toast handler
    window.showToast = function(message, isError = false) {
        const container = document.getElementById("toast-container");
        if (!container) return;
        const toast = document.createElement("div");
        toast.className = "toast-alert";
        if (isError) toast.style.backgroundColor = "#ef4444";
        toast.innerHTML = `<i class="fa-solid ${isError ? 'fa-triangle-exclamation' : 'fa-circle-check'}"></i> <span>${message}</span>`;
        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = "0";
            toast.style.transition = "opacity 0.3s ease";
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    };

    // 2. Action duplicate submit protection
    const forms = document.querySelectorAll("form");
    forms.forEach(form => {
        form.addEventListener("submit", function() {
            const submitBtns = form.querySelectorAll("button[type='submit']");
            submitBtns.forEach(btn => {
                btn.disabled = true;
                btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Procesando...`;
            });
        });
    });

    // 3. Search and filter interactive triggers
    const triggerSearch = document.getElementById("admin-search-btn");
    const searchInput = document.getElementById("admin-search-input");
    if (triggerSearch && searchInput) {
        triggerSearch.addEventListener("click", () => {
            submitFiltersForm();
        });
        searchInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                submitFiltersForm();
            }
        });
    }

    const filterStatus = document.getElementById("filter-status");
    const filterDate = document.getElementById("filter-date");
    const filterSort = document.getElementById("filter-sort");

    if (filterStatus) filterStatus.addEventListener("change", submitFiltersForm);
    if (filterDate) filterDate.addEventListener("change", submitFiltersForm);
    if (filterSort) filterSort.addEventListener("change", submitFiltersForm);

    function submitFiltersForm() {
        const form = document.getElementById("admin-filter-form");
        if (form) {
            form.submit();
        }
    }
    
    // 4. Modal actions confirmation panel
    window.confirmStateTransition = function(actionUrl, actionName, clientName, detailsText, totalValue, targetStatus) {
        const confirmModal = document.getElementById("transition-confirm-modal");
        const modalTitle = document.getElementById("confirm-modal-title");
        const modalClient = document.getElementById("confirm-modal-client");
        const modalDetails = document.getElementById("confirm-modal-details");
        const modalTotal = document.getElementById("confirm-modal-total");
        const modalForm = document.getElementById("confirm-modal-form");
        const modalStatusInput = document.getElementById("confirm-modal-status-input");
        const modalNotesTextarea = document.getElementById("confirm-modal-notes-textarea");
        const notesContainer = document.getElementById("confirm-modal-notes-container");

        if (!confirmModal) return;

        modalForm.action = actionUrl;
        modalStatusInput.value = targetStatus;
        modalTitle.textContent = `¿${actionName} este apartado?`;
        modalClient.textContent = clientName;
        modalDetails.textContent = detailsText;
        modalTotal.textContent = totalValue;

        if (targetStatus === 'CANCELADO') {
            notesContainer.style.display = "block";
            modalNotesTextarea.setAttribute("placeholder", "Escriba el motivo de la cancelación...");
        } else {
            notesContainer.style.display = "none";
        }

        confirmModal.style.display = "flex";
    };

    window.closeConfirmModal = function() {
        const confirmModal = document.getElementById("transition-confirm-modal");
        if (confirmModal) confirmModal.style.display = "none";
    };

    // Close confirm modal on escape key
    document.addEventListener("keydown", function(e) {
        if (e.key === "Escape") {
            closeConfirmModal();
            closeReservationDrawer();
        }
    });

    // 5. Drawer detail view logic
    window.openReservationDrawer = function(reservationJsonStr) {
        const data = JSON.parse(reservationJsonStr);
        const drawerOverlay = document.getElementById("detail-drawer-overlay");
        if (!drawerOverlay) return;

        document.getElementById("drawer-code").textContent = data.code;
        
        // Badge color and state
        const badge = document.getElementById("drawer-badge-status");
        badge.innerHTML = `<span>${data.statusEmoji}</span> <span>${data.statusDisplayName}</span>`;
        badge.style.backgroundColor = data.statusColor;
        badge.style.color = data.statusTextColor;

        document.getElementById("drawer-client-name").textContent = data.customerName;
        document.getElementById("drawer-client-phone").textContent = data.customerPhone;
        document.getElementById("drawer-client-email").textContent = data.customerEmail || "Sin registrar";
        document.getElementById("drawer-notes").textContent = data.notes || "Sin notas";
        document.getElementById("drawer-total").textContent = data.total;
        document.getElementById("drawer-total-words").textContent = data.totalInWords || "";
        document.getElementById("drawer-created-at").textContent = data.createdAtStr;

        // WhatsApp, Call & Email Link triggers
        const cleanPhone = data.customerPhone.replace(/[^0-9]/g, "");
        const waPhone = cleanPhone.startsWith("57") ? cleanPhone : "57" + cleanPhone;
        document.getElementById("btn-drawer-whatsapp").href = `https://wa.me/${waPhone}?text=${encodeURIComponent('Hola ' + data.customerName + ', te contactamos de Nova Droguería sobre tu apartado ' + data.code + '.')}`;
        document.getElementById("btn-drawer-call").href = `tel:${data.customerPhone}`;
        document.getElementById("btn-drawer-email").href = `mailto:${data.customerEmail || ''}`;

        // Products card list list rendering
        const productsList = document.getElementById("drawer-products-container");
        productsList.innerHTML = "";
        data.items.forEach(item => {
            const prodCard = document.createElement("div");
            prodCard.className = "card";
            prodCard.style.padding = "1rem";
            prodCard.style.margin = "0 0 1rem 0";
            prodCard.style.display = "flex";
            prodCard.style.gap = "1rem";
            prodCard.style.alignItems = "center";
            prodCard.innerHTML = `
                <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?q=80&w=100&auto=format&fit=crop'}" 
                     style="width: 50px; height: 50px; border-radius: 0.5rem; object-fit: cover; border: 1px solid var(--border-color);" alt="${item.productName}">
                <div style="flex: 1;">
                    <h4 style="margin: 0; font-size: 0.95rem; font-weight: 600; color: var(--text-dark);">${item.productName}</h4>
                    <p style="margin: 0.25rem 0 0 0; font-size: 0.85rem; color: var(--text-muted);">
                        Cantidad: <strong style="color: var(--text-dark);">${item.quantity}</strong> | Precio: <strong style="color: var(--text-dark);">${item.unitPrice}</strong>
                    </p>
                    <p style="margin: 0.25rem 0 0 0; font-size: 0.85rem; color: var(--text-muted);">
                        Subtotal: <strong style="color: var(--primary); font-weight: 700;">${item.lineTotal}</strong>
                    </p>
                </div>
            `;
            productsList.appendChild(prodCard);
        });

        // Timeline history builder
        const timelineList = document.getElementById("drawer-timeline-container");
        timelineList.innerHTML = "";
        data.history.forEach((hist, index) => {
            const isLast = index === data.history.length - 1;
            const timeItem = document.createElement("div");
            timeItem.className = `timeline-item ${isLast ? 'active' : 'completed'}`;
            timeItem.innerHTML = `
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                    <strong style="color: var(--text-dark);">${hist.displayName}</strong>
                    <span style="font-size: 0.8rem; color: var(--text-muted); display: block;">${hist.timestamp}</span>
                    <p style="margin: 0.25rem 0 0 0; font-size: 0.8rem; color: var(--text-muted); font-style: italic;">${hist.notes}</p>
                </div>
            `;
            timelineList.appendChild(timeItem);
        });

        // Copy button trigger
        const copyBtn = document.getElementById("btn-drawer-copy-code");
        copyBtn.onclick = function() {
            navigator.clipboard.writeText(data.code).then(() => {
                showToast("✓ Código copiado");
            });
        };

        // Print document trigger
        const printBtn = document.getElementById("btn-drawer-print");
        printBtn.onclick = function() {
            window.print();
        };

        // Actions toolbar rendering based on status transition constraints
        const toolbar = document.getElementById("drawer-actions-toolbar");
        toolbar.innerHTML = "";
        
        const transitionUrl = `/admin/apartados/estado/${data.id}`;
        const detailStr = `${data.items[0].productName} x${data.items[0].quantity}`;
        
        if (data.status === 'PENDIENTE') {
            toolbar.innerHTML = `
                <button class="btn btn-primary" onclick="confirmStateTransition('${transitionUrl}', 'Confirmar', '${data.customerName}', '${detailStr}', '${data.total}', 'CONFIRMADO')"><i class="fa-solid fa-check"></i> Confirmar</button>
                <button class="btn btn-danger" onclick="confirmStateTransition('${transitionUrl}', 'Cancelar', '${data.customerName}', '${detailStr}', '${data.total}', 'CANCELADO')"><i class="fa-solid fa-xmark"></i> Cancelar</button>
            `;
        } else if (data.status === 'CONFIRMADO') {
            toolbar.innerHTML = `
                <button class="btn btn-primary" style="background-color: #8b5cf6;" onclick="confirmStateTransition('${transitionUrl}', 'Preparar', '${data.customerName}', '${detailStr}', '${data.total}', 'PREPARADO')"><i class="fa-solid fa-box"></i> Listo para Recoger</button>
                <button class="btn btn-danger" onclick="confirmStateTransition('${transitionUrl}', 'Cancelar', '${data.customerName}', '${detailStr}', '${data.total}', 'CANCELADO')"><i class="fa-solid fa-xmark"></i> Cancelar</button>
            `;
        } else if (data.status === 'PREPARADO') {
            toolbar.innerHTML = `
                <button class="btn btn-primary" style="background-color: #059669;" onclick="confirmStateTransition('${transitionUrl}', 'Entregar', '${data.customerName}', '${detailStr}', '${data.total}', 'ENTREGADO')"><i class="fa-solid fa-circle-check"></i> Marcar como Entregado</button>
                <button class="btn btn-danger" onclick="confirmStateTransition('${transitionUrl}', 'Cancelar', '${data.customerName}', '${detailStr}', '${data.total}', 'CANCELADO')"><i class="fa-solid fa-xmark"></i> Cancelar</button>
            `;
        } else if (data.status === 'ENTREGADO') {
            toolbar.innerHTML = `<span style="color: #059669; font-weight: 600;"><i class="fa-solid fa-circle-check"></i> Apartado entregado</span>`;
        } else if (data.status === 'CANCELADO') {
            toolbar.innerHTML = `<span style="color: #ef4444; font-weight: 600;"><i class="fa-solid fa-circle-xmark"></i> Apartado cancelado</span>`;
        }

        drawerOverlay.style.display = "flex";
    };

    window.closeReservationDrawer = function() {
        const drawerOverlay = document.getElementById("detail-drawer-overlay");
        if (drawerOverlay) drawerOverlay.style.display = "none";
    };
});
