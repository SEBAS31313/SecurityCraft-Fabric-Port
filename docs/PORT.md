# Cambios de plataforma

El contenido se registra en las mismas claves `securitycraft:*` que el original. Los adaptadores `platform.registry` mantienen las referencias diferidas, pero escriben en los registros nativos de Minecraft. Los serializadores de entidades se registran por nombre mediante FabricEntityDataRegistry para evitar desincronización por orden de registro.

La red utiliza Fabric PayloadTypeRegistry y ServerPlayNetworking/ClientPlayNetworking. Los receptores comunes y de cliente se registran por separado. Los códigos, las sales, los propietarios y sus datos NBT conservan el esquema del original; los datos de contraseña destinados solo al servidor se mantienen fuera de las actualizaciones de cliente.

Las capacidades de inventario se sustituyen por Fabric Transfer API. Los adaptadores de slots, cofres dobles y transacciones conservan las restricciones de inserción/extracción. Los proveedores se seleccionan por el tipo real de entidad de bloque, incluyendo estados compartidos de pistones en movimiento.

Los menús transportan su contexto mediante ExtendedMenuType y ExtendedMenuProvider. Los eventos de interacción, daño, objetivos, teletransporte y ciclo de vida se conectan a callbacks de Fabric y a mixins donde no existe un callback equivalente. Los métodos añadidos a vanilla por NeoForge se sustituyen por interfaces y puntos de inyección concretos.

El cliente utiliza datos de render comunes y la API de renderizado de Fabric para los disfraces. Se adaptaron los registros de renderizadores, las capas HUD, las pantallas, la pose del taser y las cámaras. La ampliación de enum de NeoForge para la pose del taser se sustituye por la transformación de la pose en el modelo humanoide.

Los access transformers originales se convierten en reglas de acceso de Fabric en namespace `official`. Se conserva el permiso de sobrescritura donde el original lo necesita. Las recetas de fluidos falsos usan el soporte nativo de pociones con inyecciones para aceptar cubos e ingredientes; los cubos exponen almacenamiento transaccional de fluidos de Fabric.

Los generadores de datos de NeoForge no forman parte del runtime. Los recursos generados tienen prioridad sobre los siete recursos duplicados de la carpeta principal, reproduciendo el contenido del JAR original.
