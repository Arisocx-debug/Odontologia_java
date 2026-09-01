// --- Carrito en localStorage ---
function getCart() {
  const cart = localStorage.getItem('cart');
  return cart ? JSON.parse(cart) : [];
}

function saveCart(cart) {
  localStorage.setItem('cart', JSON.stringify(cart));
}

// --- Agregar al carrito ---
function addToCart(product) {
  const cart = getCart();
  const existing = cart.find(item => item.id === product.id);

  if (existing) {
    existing.quantity += 1;
  } else {
    cart.push({ ...product, quantity: 1 });
  }

  saveCart(cart);
  showToast(`${product.name} agregado al carrito`);

  const stockElement = document.querySelector(`[data-id="${product.id}"]`)
    ?.closest('.card-body')
    ?.querySelector('.text-muted');

  if (stockElement) {
    const currentStock = parseInt(stockElement.textContent.replace(/\D/g, ''));
    const newStock = currentStock - 1;
    stockElement.textContent = `Stock disponible: ${newStock}`;
  }

  // Descontar 1 en backend
  fetch('/cliente/actualizar-stock', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `idInventario=${product.id}&cantidad=1`
  });
}

// --- Toast ---
function showToast(message) {
  const toast = document.createElement('div');
  toast.textContent = message;
  toast.className = 'toast-message';
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 2000);
}

// --- Render carrito ---
function renderCart() {
  const cart = getCart();
  const tbody = document.getElementById('cart-items');
  const totalEl = document.getElementById('cart-total');
  if (!tbody || !totalEl) return;

  tbody.innerHTML = '';
  let total = 0;

  cart.forEach(item => {
    const subtotal = item.price * item.quantity;
    total += subtotal;

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${item.name}</td>
      <td>
        <input type="number" min="1" value="${item.quantity}" data-id="${item.id}" class="form-control qty-input">
      </td>
      <td>$${item.price.toLocaleString()}</td>
      <td>$${subtotal.toLocaleString()}</td>
      <td>
        <button class="btn btn-danger btn-sm btn-remove" data-id="${item.id}">
          <i class="fa-solid fa-trash"></i>
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  totalEl.textContent = `$${total.toLocaleString()}`;

  document.querySelectorAll('.qty-input').forEach(input => {
    input.addEventListener('change', e => {
      const id = e.target.dataset.id;
      const qty = Number(e.target.value);
      updateQuantity(id, qty);
    });
  });

  document.querySelectorAll('.btn-remove').forEach(btn => {
    btn.addEventListener('click', e => {
      const id = e.target.dataset.id;
      removeFromCart(id);
    });
  });
}

function updateQuantity(id, quantity) {
  const cart = getCart();
  const item = cart.find(i => i.id === id);
  if (item) {
    item.quantity = quantity;
    saveCart(cart);
    renderCart();
  }
}

function removeFromCart(id) {
  let cart = getCart();
  cart = cart.filter(item => item.id !== id);
  saveCart(cart);
  renderCart();
}

// --- Total en checkout ---
function mostrarTotal() {
  const cart = getCart();
  let total = 0;
  cart.forEach(item => total += item.price * item.quantity);
  const totalEl = document.getElementById('total-pagar');
  if (totalEl) totalEl.textContent = `$${total.toLocaleString()}`;
}

// --- Inicialización ---
document.addEventListener('DOMContentLoaded', () => {
  // Botones "Agregar al carrito" en inventario
  const addButtons = document.querySelectorAll('.add-to-cart');
  addButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const product = {
        id: btn.dataset.id,
        name: btn.dataset.name,
        price: Number(btn.dataset.price),
        stock: Number(btn.dataset.stock)
      };
      addToCart(product);
    });
  });

  // Página carrito
  if (document.getElementById('cart-items')) {
    renderCart();
  }

  // Página pago
  if (document.getElementById('total-pagar')) {
    mostrarTotal();

    const form = document.querySelector('form');
    if (form) {
      form.addEventListener('submit', e => {
        e.preventDefault();

        const cart = getCart();
        if (cart.length === 0) {
          showToast('🛒 No hay productos en el carrito');
          return;
        }

        // Descontar stock según cantidad
        cart.forEach(item => {
          fetch('/cliente/actualizar-stock', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `idInventario=${item.id}&cantidad=${item.quantity}`
          });
        });

        // TODO: aquí deberías llamar a tu backend de "ventas" para aumentar las ventas
        // Ejemplo:
        // fetch('/cliente/registrar-venta', { ... })

        showToast('✅ Compra exitosa');
        localStorage.removeItem('cart');

        setTimeout(() => {
          window.location.href = '/cliente/inventario';
        }, 2000);
      });
    }
  }
});



