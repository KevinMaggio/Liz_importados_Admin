# Estructura recomendada de Firestore para LizImportadosAdmin

## Colección: `ventas`
Cada documento representa una venta realizada.
- `fecha`: timestamp (fecha y hora de la venta)
- `productos`: array de objetos `{ productoId, cantidad, precioUnitario }`
- `total`: número (total de la venta)
- `usuario`: string (opcional, quién realizó la venta)

## Colección: `productos`
Cada documento representa un producto.
- `nombre`: string
- `stock`: número
- `vendidos`: número (opcional, acumulado)
- `activo`: boolean

## Colección: `combos`
Cada documento representa un combo de productos.
- `nombre`: string
- `stock`: número
- `vendidos`: número (opcional, acumulado)
- `activo`: boolean

---

> Esta estructura permite obtener métricas, ventas semanales y alertas inteligentes de manera eficiente para la Home de la app. 