document.querySelectorAll("form.eliminar-form").forEach(form => {
    form.addEventListener("submit", function(e) {
        if (!confirm("¿Seguro que deseas eliminar este producto?")) {
            e.preventDefault();
        }
    });
});

