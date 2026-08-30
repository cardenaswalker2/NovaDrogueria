document.addEventListener("DOMContentLoaded", function() {
    const menuToggle = document.querySelector(".menu-toggle");
    const navbarNav = document.getElementById("navbar-nav");
    
    if (menuToggle && navbarNav) {
        // Remove direct inline click to comply with strict CSP policies
        menuToggle.removeAttribute("onclick");
        
        menuToggle.addEventListener("click", function(e) {
            e.stopPropagation();
            navbarNav.classList.toggle("mobile-active");
        });

        // Close mobile menu if clicked outside
        document.addEventListener("click", function(e) {
            if (navbarNav.classList.contains("mobile-active") && !navbarNav.contains(e.target) && !menuToggle.contains(e.target)) {
                navbarNav.classList.remove("mobile-active");
            }
        });

        // Close on ESC key
        document.addEventListener("keydown", function(e) {
            if (e.key === "Escape" && navbarNav.classList.contains("mobile-active")) {
                navbarNav.classList.remove("mobile-active");
            }
        });
    }
});
