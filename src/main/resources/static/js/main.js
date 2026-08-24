// static/js/main.js
document.addEventListener('DOMContentLoaded', () => {
  const deleteForms = document.querySelectorAll('form[action*="/inventario/"]');
  deleteForms.forEach(form => {
    form.addEventListener('submit', e => {
      if (!confirm('¿Seguro que deseas eliminar este producto?')) {
        e.preventDefault();
      }
    });
  });
});
