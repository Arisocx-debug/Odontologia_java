package com.wilsonmontenegro.odontologia.bootstrap;

import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.model.Cliente;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.model.Producto;
import com.wilsonmontenegro.odontologia.model.Proveedor;
import com.wilsonmontenegro.odontologia.model.Servicio;
import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;
import com.wilsonmontenegro.odontologia.model.enums.EstadoInventario;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import com.wilsonmontenegro.odontologia.repository.CitaRepository;
import com.wilsonmontenegro.odontologia.repository.ClienteRepository;
import com.wilsonmontenegro.odontologia.repository.InventarioRepository;
import com.wilsonmontenegro.odontologia.repository.ProductoRepository;
import com.wilsonmontenegro.odontologia.repository.ProveedorRepository;
import com.wilsonmontenegro.odontologia.repository.ServicioRepository;
import com.wilsonmontenegro.odontologia.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inserta muchos registros de una sola corrida (carga masiva a nivel de aplicacion).
 * Usa saveAll por lotes y vacia el contexto de persistencia para no saturar el Heap.
 * Solo corre si la tabla users esta vacia, para no duplicar datos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargaMasivaService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ServicioRepository servicioRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final CitaRepository citaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Value("${app.carga-masiva.tamano-lote:25}")
    private int tamanoLote;

    @Value("${app.carga-masiva.cantidad-clientes:40}")
    private int cantidadClientes;

    @Transactional
    public void ejecutarSiBaseVacia() {
        if (usuarioRepository.count() > 0) {
            log.info("Carga masiva omitida: la base ya tiene usuarios.");
            return;
        }

        // Un solo BCrypt para todos los usuarios demo: hashear 40 veces seria lento a proposito.
        String claveHash = passwordEncoder.encode("Demo123*");

        cargarUsuariosYClientes(claveHash);
        List<Servicio> servicios = cargarServicios();
        cargarCatalogoEInventario();
        cargarCitas(servicios);

        log.info("Carga masiva terminada. Login demo: admin@wilsonmontenegro.com / Demo123*");
    }

    private void cargarUsuariosYClientes(String claveHash) {
        List<Usuario> staff = List.of(
                usuario("Admin Demo", "admin@wilsonmontenegro.com", "3000000001", Rol.ADMINISTRADOR, claveHash),
                usuario("Empleado Demo", "empleado@wilsonmontenegro.com", "3000000002", Rol.EMPLEADO, claveHash)
        );
        usuarioRepository.saveAll(staff);

        List<Usuario> clientesUsuario = new ArrayList<>();
        for (int i = 1; i <= cantidadClientes; i++) {
            clientesUsuario.add(usuario(
                    "Cliente Demo " + i,
                    "cliente" + i + "@wilsonmontenegro.com",
                    "31000000" + String.format("%02d", i % 100),
                    Rol.CLIENTE,
                    claveHash
            ));
        }
        guardarEnLotes(clientesUsuario, usuarioRepository);

        List<Cliente> fichas = new ArrayList<>();
        List<Usuario> persistidos = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Rol.CLIENTE)
                .toList();
        for (Usuario u : persistidos) {
            fichas.add(Cliente.builder().usuario(u).build());
        }
        guardarEnLotes(fichas, clienteRepository);
        log.info("Usuarios insertados: {}, clientes: {}", usuarioRepository.count(), clienteRepository.count());
    }

    private List<Servicio> cargarServicios() {
        List<Servicio> servicios = List.of(
                servicio("Consulta general", "Valoracion odontologica inicial", "80000"),
                servicio("Limpieza dental", "Profilaxis y detartraje", "120000"),
                servicio("Blanqueamiento", "Blanqueamiento en consultorio", "250000"),
                servicio("Ortodoncia control", "Control de brackets", "90000"),
                servicio("Extraccion simple", "Extraccion de pieza dental", "150000"),
                servicio("Resina", "Obturacion en resina", "110000"),
                servicio("Endodoncia", "Tratamiento de conductos", "380000"),
                servicio("Implante", "Implante dental unitario", "1800000")
        );
        List<Servicio> guardados = servicioRepository.saveAll(servicios);
        entityManager.flush();
        log.info("Servicios insertados: {}", guardados.size());
        return guardados;
    }

    private void cargarCatalogoEInventario() {
        Proveedor dental = proveedorRepository.save(Proveedor.builder()
                .nombre("Dental Andes")
                .contacto("Laura Perez")
                .telefono("6015550101")
                .email("ventas@dentalandes.com")
                .direccion("Bogota")
                .build());
        Proveedor biomax = proveedorRepository.save(Proveedor.builder()
                .nombre("BioMax Insumos")
                .contacto("Carlos Ruiz")
                .telefono("6015550202")
                .email("contacto@biomax.com")
                .direccion("Medellin")
                .build());

        List<Producto> productos = List.of(
                producto("Guantes nitrilo", "MediGlove", "35000", dental),
                producto("Mascarillas", "SafeMask", "28000", dental),
                producto("Anestesia local", "AnesFarma", "45000", biomax),
                producto("Resina composite", "3M", "95000", biomax),
                producto("Algodon", "CottonPro", "12000", dental),
                producto("Hilo dental", "OralB", "18000", dental)
        );
        productos = productoRepository.saveAll(productos);

        List<Inventario> stock = new ArrayList<>();
        for (Producto p : productos) {
            stock.add(Inventario.builder()
                    .nombre(p.getNombre())
                    .stock(50 + p.getNombre().length())
                    .precioUnitario(p.getPrecio())
                    .nombreProveedor(p.getProveedor().getNombre())
                    .producto(p)
                    .descripcion("Carga masiva de demostracion")
                    .ultimaActualizacion(LocalDateTime.now())
                    .estado(EstadoInventario.ACTIVO)
                    .build());
        }
        inventarioRepository.saveAll(stock);
        entityManager.flush();
        log.info("Proveedores, productos e inventario insertados.");
    }

    private void cargarCitas(List<Servicio> servicios) {
        List<Cliente> clientes = clienteRepository.findAll();
        if (clientes.isEmpty() || servicios.isEmpty()) {
            return;
        }

        EstadoCita[] estados = {EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.ATENDIDA, EstadoCita.CANCELADA};
        List<Cita> citas = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0).plusDays(1);

        int total = Math.min(30, clientes.size());
        for (int i = 0; i < total; i++) {
            LocalDateTime entrada = base.plusDays(i).withHour(8 + (i % 8));
            citas.add(Cita.builder()
                    .fechaEntrada(entrada)
                    .fechaSalida(entrada.plusHours(1))
                    .estado(estados[i % estados.length])
                    .tipo("Consulta")
                    .cliente(clientes.get(i))
                    .servicio(servicios.get(i % servicios.size()))
                    .build());
        }
        guardarEnLotes(citas, citaRepository);
        log.info("Citas insertadas: {}", total);
    }

    private <T> void guardarEnLotes(List<T> entidades, org.springframework.data.jpa.repository.JpaRepository<T, Long> repo) {
        for (int i = 0; i < entidades.size(); i += tamanoLote) {
            int fin = Math.min(i + tamanoLote, entidades.size());
            repo.saveAll(entidades.subList(i, fin));
            entityManager.flush();
            entityManager.clear();
        }
    }

    private static Usuario usuario(String nombre, String email, String telefono, Rol rol, String hash) {
        return Usuario.builder()
                .name(nombre)
                .email(email)
                .telefono(telefono)
                .rol(rol)
                .password(hash)
                .build();
    }

    private static Servicio servicio(String nombre, String descripcion, String costo) {
        return Servicio.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .costo(new BigDecimal(costo))
                .build();
    }

    private static Producto producto(String nombre, String marca, String precio, Proveedor proveedor) {
        return Producto.builder()
                .nombre(nombre)
                .marca(marca)
                .precio(new BigDecimal(precio))
                .descripcion("Insumo de consultorio")
                .proveedor(proveedor)
                .build();
    }
}
