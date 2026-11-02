# SecurityCraft — Unofficial Fabric Port

Independent beta port of SecurityCraft 1.10.2.1 for Minecraft 26.2 and Fabric.
Protect your base with security cameras, monitors, sentries, reinforced blocks,
keypads, keycards, traps, and security modules. More updates and Minecraft
versions are planned.

This project is not affiliated with or endorsed by the original SecurityCraft team.

## Requirements and installation

- Minecraft 26.2, Java 25, and Fabric Loader 0.19.5 or newer.
- Fabric API for Minecraft 26.2 (minimum 0.155.2; also tested with 0.161.0).
- Forge Config API Port 26.2.1 **for Fabric**.

Install the mod and both dependencies on the server and every client.
Use one Fabric API version; keep 0.161.0 if your modpack already includes it.

## Build from source

Install a JDK 25 and ensure `JAVA_HOME` points to it. From the repository root:

```sh
./gradlew build
```

On Windows, use `gradlew.bat build`. The Gradle wrapper downloads Gradle and
build dependencies automatically. An internet connection is needed for the first build.
The distributable JAR is generated in `build/libs/`; the `-sources.jar` is for developers.

To build against the Fabric API version used by the Primezz profile:

```sh
./gradlew build -Psc_test_fabric_api=0.161.0+26.2
```

## Tests and development

```sh
./gradlew test
./gradlew runGameTest
./gradlew runClientGameTest
```

Client game tests require a graphical session. Game tests use isolated development
worlds. The development runs accept the Minecraft EULA through the Fabric test
configuration; review it before running them.

Previously completed checks for this beta: a successful build, 7 JUnit transfer
contract tests, 18 server game tests, client camera/UI/taser checks, and a packaged
dedicated-server startup. Modpack testing had exclusions: Essential and Xaero
could not be certified in the automated harness; Metallum with Iris failed in
the tested client. Solas fell back to vanilla rendering and is **not validated**.
These results do not establish compatibility with every modpack or shader.

`src/test` and `src/gametest` are excluded from the distributable mod. Original
NeoForge data generators remain as reference; the build uses the generated resources.
See [port notes](docs/PORT.md) for implementation details.

## Credits and license

Original SecurityCraft by Geforce, bl4ckscor3, Redstone_Dubstep, ChainmailPickaxe,
and the SecurityCraft development team.

- [Original source](https://github.com/Geforce132/SecurityCraft/tree/26.2), baseline
  commit `b88421b815599b60dd3c2de4b185ecce72e6ed9b`.
- [Community Fabric reference](https://github.com/VerkoSK/SecurityCraftFabric/tree/26.2),
  commit `cb13500d5b9fac48e668d13aa7559eb613bd2fdc`.
- [Original README](docs/README_UPSTREAM.md).

Licensed under [MIT](LICENSE). Preserve the original license and copyright notice.
Dependencies retain their own licenses and are not bundled in this repository.

## Español

Port independiente y no oficial en beta de SecurityCraft para Fabric 26.2.
Incluye cámaras, monitores, torretas, bloques reforzados, controles de acceso y
trampas. Se planean más actualizaciones y versiones de Minecraft.

Necesita Java 25, Fabric Loader 0.19.5 o superior, Fabric API para 26.2 y
Forge Config API Port 26.2.1 para Fabric. Instala el mod y las dependencias
tanto en servidor como en clientes. Compila con `./gradlew build` o
`gradlew.bat build`; encontrarás el JAR en `build/libs/`.

Es una beta: consulta las limitaciones anteriores antes de usarla en tu SMP.
