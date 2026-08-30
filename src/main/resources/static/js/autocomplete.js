document.addEventListener("DOMContentLoaded", function() {
    const searchInputs = document.querySelectorAll("input[name='buscar']");
    
    searchInputs.forEach(input => {
        // Find existing .search-wrapper container, or fallback
        let wrapper = input.closest(".search-wrapper");
        if (!wrapper) {
            wrapper = document.createElement("div");
            wrapper.className = "search-wrapper";
            input.parentNode.insertBefore(wrapper, input);
            wrapper.appendChild(input);
        }
        
        // Construct clean inner controls without replacing/moving original elements out of style flow
        const inputContainer = document.createElement("div");
        inputContainer.className = "search-input-container";
        wrapper.insertBefore(inputContainer, input);
        
        // Add icons inside input container
        const searchIcon = document.createElement("i");
        searchIcon.className = "fa-solid fa-magnifying-glass search-icon-inside";
        inputContainer.appendChild(searchIcon);
        
        // Move input inside the helper container
        input.style.paddingLeft = "2.75rem";
        input.style.paddingRight = "3.5rem";
        inputContainer.appendChild(input);
        
        // Spinner indicator
        const spinner = document.createElement("div");
        spinner.className = "search-spinner";
        inputContainer.appendChild(spinner);
        
        // Clear button
        const clearBtn = document.createElement("button");
        clearBtn.type = "button";
        clearBtn.className = "search-clear-btn";
        clearBtn.innerHTML = '<i class="fa-solid fa-xmark"></i>';
        inputContainer.appendChild(clearBtn);
        
        // Dropdown suggestions panel
        const dropdown = document.createElement("div");
        dropdown.className = "search-dropdown";
        dropdown.setAttribute("role", "listbox");
        dropdown.setAttribute("aria-label", "Sugerencias de búsqueda");
        wrapper.appendChild(dropdown);
        
        let debounceTimeout;
        let activeIndex = -1;
        let abortController = null;
        
        // Set aria properties
        input.setAttribute("aria-autocomplete", "list");
        input.setAttribute("aria-expanded", "false");
        
        if (input.value.trim().length > 0) {
            clearBtn.style.display = "flex";
        }
        
        input.addEventListener("input", function() {
            const value = input.value.trim();
            if (value.length > 0) {
                clearBtn.style.display = "flex";
            } else {
                clearBtn.style.display = "none";
            }
            
            clearTimeout(debounceTimeout);
            activeIndex = -1;
            
            if (abortController) {
                abortController.abort();
            }
            
            if (value.length < 1) {
                dropdown.style.display = "none";
                dropdown.innerHTML = "";
                input.setAttribute("aria-expanded", "false");
                return;
            }
            
            // Show immediate loading indicator state
            dropdown.innerHTML = `
                <div class="search-dropdown-empty">
                    <i class="fa-solid fa-circle-notch fa-spin" style="color: var(--primary);"></i>
                    <p style="margin: 0.5rem 0 0 0; font-weight: 600; color: var(--text-dark);">Buscando productos...</p>
                </div>
            `;
            dropdown.style.display = "block";
            input.setAttribute("aria-expanded", "true");
            
            debounceTimeout = setTimeout(() => {
                spinner.style.display = "block";
                abortController = new AbortController();
                const signal = abortController.signal;
                
                fetch(`/api/productos/buscar?q=${encodeURIComponent(value)}`, { signal })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("HTTP Status: " + response.status);
                        }
                        return response.json();
                    })
                    .then(data => {
                        spinner.style.display = "none";
                        renderDropdown(data, value);
                    })
                    .catch(err => {
                        if (err.name === 'AbortError') return;
                        spinner.style.display = "none";
                        dropdown.innerHTML = `
                            <div class="search-dropdown-empty">
                                <i class="fa-solid fa-triangle-exclamation" style="color: #ef4444;"></i>
                                <p style="margin: 0.5rem 0 0 0; font-weight: 600; color: #ef4444;">No pudimos realizar la búsqueda. Intenta nuevamente.</p>
                            </div>
                        `;
                    });
            }, 250);
        });
        
        clearBtn.addEventListener("click", function() {
            input.value = "";
            clearBtn.style.display = "none";
            dropdown.style.display = "none";
            dropdown.innerHTML = "";
            input.setAttribute("aria-expanded", "false");
            input.focus();
        });
        
        // Keyboard navigation
        input.addEventListener("keydown", function(e) {
            const items = dropdown.querySelectorAll(".search-dropdown-item, .search-dropdown-footer");
            if (items.length === 0) return;
            
            if (e.key === "ArrowDown") {
                e.preventDefault();
                activeIndex = (activeIndex + 1) % items.length;
                highlightItem(items);
            } else if (e.key === "ArrowUp") {
                e.preventDefault();
                activeIndex = (activeIndex - 1 + items.length) % items.length;
                highlightItem(items);
            } else if (e.key === "Enter") {
                if (activeIndex > -1) {
                    e.preventDefault();
                    items[activeIndex].click();
                }
            } else if (e.key === "Escape") {
                dropdown.style.display = "none";
                input.setAttribute("aria-expanded", "false");
            }
        });
        
        function highlightItem(items) {
            items.forEach((item, index) => {
                if (index === activeIndex) {
                    item.classList.add("keyboard-selected");
                    item.focus();
                } else {
                    item.classList.remove("keyboard-selected");
                }
            });
        }
        
        function renderDropdown(products, queryText) {
            dropdown.innerHTML = "";
            if (products.length === 0) {
                dropdown.innerHTML = `
                    <div class="search-dropdown-empty">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <p style="margin: 0.5rem 0 0 0; font-weight: 600; color: var(--text-dark);">No encontramos productos para "${queryText}"</p>
                        <p style="margin: 0.25rem 0 0 0; font-size: 0.85rem;">Prueba con otro nombre, marca o principio activo.</p>
                    </div>
                `;
                return;
            }
            
            products.forEach(product => {
                let statusText = "● Disponible";
                let statusClass = "status-available";
                if (product.stock === 0) {
                    statusText = "● Agotado";
                    statusClass = "status-out";
                } else if (product.stock <= 5) {
                    statusText = "● Poco stock";
                    statusClass = "status-low-stock";
                }
                
                const item = document.createElement("div");
                item.className = "search-dropdown-item";
                item.setAttribute("role", "option");
                item.tabIndex = 0;
                item.innerHTML = `
                    <img src="${product.imageUrl}" class="search-dropdown-img" alt="${product.name}">
                    <div class="search-dropdown-info">
                        <p class="search-dropdown-name">${product.name}</p>
                        <p class="search-dropdown-meta">
                            <span>${product.presentation}</span>
                            <span style="font-weight: 700; color: var(--text-dark);">$${product.price}</span>
                        </p>
                    </div>
                    <span class="search-dropdown-status ${statusClass}">${statusText}</span>
                `;
                item.addEventListener("click", () => {
                    window.location.href = `/productos/${product.slug}`;
                });
                dropdown.appendChild(item);
            });
            
            const footer = document.createElement("a");
            footer.href = `/catalogo?buscar=${encodeURIComponent(queryText)}`;
            footer.className = "search-dropdown-footer";
            footer.tabIndex = 0;
            footer.innerHTML = `Ver todos los resultados para "${queryText}" →`;
            dropdown.appendChild(footer);
        }
        
        // Close dropdown on click outside
        document.addEventListener("click", function(e) {
            if (!wrapper.contains(e.target)) {
                dropdown.style.display = "none";
                input.setAttribute("aria-expanded", "false");
            }
        });
    });
});
