// ============================================================
// CARRITO EN LOCALSTORAGE
// ============================================================

function getCart() {

    try {

        const cart = localStorage.getItem('cart');

        if (!cart) {
            return [];
        }

        const parsedCart = JSON.parse(cart);

        return Array.isArray(parsedCart)
            ? parsedCart
            : [];

    } catch (error) {

        console.error('Error al leer el carrito:', error);

        return [];
    }
}


// ============================================================
// GUARDAR CARRITO
// ============================================================

function saveCart(cart) {

    try {

        localStorage.setItem(
            'cart',
            JSON.stringify(cart)
        );

    } catch (error) {

        console.error('Error al guardar el carrito:', error);

        showToast('No fue posible guardar el carrito.');
    }
}


// ============================================================
// AGREGAR PRODUCTO AL CARRITO
// ============================================================

function addToCart(product) {

    const cart = getCart();

    const productId = String(product.id);

    const stock = Number(product.stock);

    const price = Number(product.price);


    // --------------------------------------------------------
    // Validar producto
    // --------------------------------------------------------

    if (!product.id) {

        showToast('Producto inválido.');

        return;
    }


    if (!Number.isFinite(stock) || stock <= 0) {

        showToast('❌ Este producto está agotado.');

        return;
    }


    if (!Number.isFinite(price) || price < 0) {

        showToast('El precio del producto no es válido.');

        return;
    }


    // --------------------------------------------------------
    // Buscar producto existente
    // --------------------------------------------------------

    const existing = cart.find(
        item => String(item.id) === productId
    );


    // --------------------------------------------------------
    // Verificar stock
    // --------------------------------------------------------

    if (
        existing &&
        Number(existing.quantity) >= stock
    ) {

        showToast(
            'No puedes agregar más unidades que el stock disponible.'
        );

        return;
    }


    // --------------------------------------------------------
    // Incrementar cantidad
    // --------------------------------------------------------

    if (existing) {

        existing.quantity =
            Number(existing.quantity) + 1;

        // Actualizar stock por si cambió en la página
        existing.stock = stock;

    } else {

        cart.push({

            id: productId,

            name: String(product.name || 'Producto'),

            price: price,

            stock: stock,

            quantity: 1

        });

    }


    saveCart(cart);

    renderCart();

    mostrarTotal();


    showToast(
        `${product.name} agregado al carrito`
    );
}


// ============================================================
// TOAST
// ============================================================

function showToast(message) {

    const toast = document.createElement('div');

    toast.textContent = message;

    toast.className = 'toast-message';

    document.body.appendChild(toast);


    setTimeout(() => {

        toast.remove();

    }, 2000);
}


// ============================================================
// RENDERIZAR CARRITO
// ============================================================

function renderCart() {

    const cart = getCart();

    const tbody =
        document.getElementById('cart-items');

    const totalEl =
        document.getElementById('cart-total');


    if (!tbody || !totalEl) {

        return;
    }


    tbody.innerHTML = '';


    let total = 0;


    // --------------------------------------------------------
    // Carrito vacío
    // --------------------------------------------------------

    if (cart.length === 0) {

        tbody.innerHTML = `

            <tr>

                <td
                    colspan="5"
                    class="text-center text-muted py-4"
                >

                    <i class="fa-solid fa-cart-shopping fa-2x mb-2"></i>

                    <br>

                    Tu carrito está vacío.

                </td>

            </tr>

        `;

        totalEl.textContent = '$0';

        return;
    }


    // --------------------------------------------------------
    // Productos
    // --------------------------------------------------------

    cart.forEach(item => {

        const price =
            Number(item.price) || 0;

        const quantity =
            Number(item.quantity) || 1;

        const stock =
            Number(item.stock) || 0;


        const subtotal =
            price * quantity;


        total += subtotal;


        const tr =
            document.createElement('tr');


        tr.innerHTML = `

            <td>
                ${escapeHtml(item.name)}
            </td>

            <td>

                <input
                    type="number"
                    min="1"
                    max="${stock}"
                    value="${quantity}"
                    data-id="${item.id}"
                    class="form-control qty-input"
                >

            </td>

            <td>
                $${price.toLocaleString('es-CO')}
            </td>

            <td>
                $${subtotal.toLocaleString('es-CO')}
            </td>

            <td>

                <button
                    type="button"
                    class="btn btn-danger btn-sm btn-remove"
                    data-id="${item.id}"
                    title="Eliminar producto"
                >

                    <i class="fa-solid fa-trash"></i>

                </button>

            </td>

        `;


        tbody.appendChild(tr);

    });


    // --------------------------------------------------------
    // Total
    // --------------------------------------------------------

    totalEl.textContent =
        `$${total.toLocaleString('es-CO')}`;


    // --------------------------------------------------------
    // Eventos cantidad
    // --------------------------------------------------------

    document
        .querySelectorAll('.qty-input')
        .forEach(input => {

            input.addEventListener(
                'change',
                event => {

                    const id =
                        event.target.dataset.id;

                    const quantity =
                        Number(event.target.value);

                    updateQuantity(
                        id,
                        quantity
                    );

                }
            );

        });


    // --------------------------------------------------------
    // Eventos eliminar
    // --------------------------------------------------------

    document
        .querySelectorAll('.btn-remove')
        .forEach(button => {

            button.addEventListener(
                'click',
                event => {

                    const id =
                        event.currentTarget.dataset.id;

                    removeFromCart(id);

                }
            );

        });

}


// ============================================================
// ACTUALIZAR CANTIDAD
// ============================================================

function updateQuantity(id, quantity) {

    const cart = getCart();


    const item = cart.find(
        item =>
            String(item.id) === String(id)
    );


    if (!item) {

        return;
    }


    const stock =
        Number(item.stock);


    // --------------------------------------------------------
    // Validar cantidad
    // --------------------------------------------------------

    if (
        !Number.isInteger(quantity) ||
        quantity < 1
    ) {

        quantity = 1;
    }


    // --------------------------------------------------------
    // Verificar stock
    // --------------------------------------------------------

    if (
        !Number.isFinite(stock) ||
        stock <= 0
    ) {

        removeFromCart(id);

        showToast(
            'Este producto ya no tiene stock disponible.'
        );

        return;
    }


    if (quantity > stock) {

        showToast(
            'La cantidad supera el stock disponible.'
        );

        quantity = stock;
    }


    // --------------------------------------------------------
    // Guardar
    // --------------------------------------------------------

    item.quantity = quantity;

    saveCart(cart);

    renderCart();

    mostrarTotal();
}


// ============================================================
// ELIMINAR DEL CARRITO
// ============================================================

function removeFromCart(id) {

    let cart = getCart();


    cart = cart.filter(
        item =>
            String(item.id) !== String(id)
    );


    saveCart(cart);

    renderCart();

    mostrarTotal();

}


// ============================================================
// TOTAL DEL CHECKOUT
// ============================================================

function mostrarTotal() {

    const cart = getCart();

    let total = 0;


    cart.forEach(item => {

        const price =
            Number(item.price) || 0;

        const quantity =
            Number(item.quantity) || 0;


        total +=
            price * quantity;

    });


    const totalEl =
        document.getElementById('total-pagar');


    if (totalEl) {

        totalEl.textContent =
            `$${total.toLocaleString('es-CO')}`;

    }
}


// ============================================================
// ESCAPAR HTML
// ============================================================

function escapeHtml(value) {

    const div =
        document.createElement('div');

    div.textContent =
        value ?? '';

    return div.innerHTML;
}


// ============================================================
// INICIALIZACIÓN
// ============================================================

document.addEventListener(
    'DOMContentLoaded',
    () => {

        // ====================================================
        // BOTONES "AGREGAR AL CARRITO"
        // ====================================================

        const addButtons =
            document.querySelectorAll(
                '.add-to-cart'
            );


        addButtons.forEach(button => {

            button.addEventListener(
                'click',
                () => {

                    const product = {

                        id:
                            button.dataset.id,

                        name:
                            button.dataset.name,

                        price:
                            Number(
                                button.dataset.price
                            ),

                        stock:
                            Number(
                                button.dataset.stock
                            )

                    };


                    addToCart(product);

                }
            );

        });


        // ====================================================
        // PÁGINA CARRITO
        // ====================================================

        if (
            document.getElementById(
                'cart-items'
            )
        ) {

            renderCart();

        }


        // ====================================================
        // PÁGINA CHECKOUT
        // ====================================================

        if (
            document.getElementById(
                'total-pagar'
            )
        ) {

            mostrarTotal();

        }

    }
);


