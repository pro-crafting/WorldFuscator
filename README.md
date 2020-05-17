[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.pro-crafting.mc/WorldFuscator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.pro-crafting.mc/WorldFuscator)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# `WorldFuscator`

Fuscation for everybody and everything.

## What?

Worldfuscator allows to hide blocks from view, by replacing them by a given other material.
This works based on permissions, and, depending on which plugin is installed, either the WGK Teams, or on the Worldguard Regions.

This plugin was originally used on MyPlayPlanet.net, and is now provided by the original author for everybody.

## Features

* High Performance fuscation of blocks
* Automatic refresh of fuscated blocks based on BlockRegion or Wargear events
* MC 1.13 compatible
* Fuscates to Ender Stone (Changeable in the configuration)

## Debug Mode
The debug mode prints information about the fuscation in the console. It also allows to save chunks as .mcdp files.
.mcdp files contain every information sent by the server about a chunk saved into a file.
This includes:
* Blocks & Block State
* Lighting
* Tile Entities
* Biome

At the same time, .chunk files are written. These contain only informaion about the blocks.

This is enabled either by changing the configuration file, or by wielding the mighty BLAZE_ROD.

The files are saved in `plugins/WorldFuscator/chunks/<worldname>`

## Example Configuration
```
debug:
  enabled: false
fuscation-mode: CHUNK_AND_BLOCK
async:
  worker-count: 0
preferred-obfuscation-material: 'minecraft:end_stone'
hidden:
  block:
    entities:
      - 'minecraft:banner'
    filters:
      - 'minecraft:red_wool
      - 'minecraft:sticky_piston[extended=true]'
      - '[waterlogged=true]
```

All blocks matching the filters will be replaced by end stone.
All block entities will be replaced by air.

WorldFuscator can optionally run in async mode. Specifiy the number of worker threads for this feature. `worker-count` of 0 means, that the plugin will completly run on the main thread.
You should not have more workers than you have system threads available. 

### Fuscation Modes
* CHUNK_AND_BLOCK `Stable`

In this fuscation mode, chunk and blocks are checked if the player has rights. This allows for perfect fuscation in tight spaces.

## Integrations
Currently, WorldFuscator has Integration Plugins for following region providers:
* Wargear Fight Plugin
* WorldGuard

### WorldGuard Integration
Regions you are recently made a member of are only taken account after a new login onto the server. 
If you want to refresh worldguard regions on the fly, you can call the WorldFuscatorImpl.updateRegion method.

## Building:

The project is built using maven.

The following maven phase will create a plugin.jar file you can use in your server directly
````
mvn clean install
````

This requires an installed java jdk 8 and installed maven.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/Postremus/xmlpretty/tags). 

## Authors

* **Martin Panzer** - *Initial work* - [Postremus](https://github.com/Postremus)
* **MyPlayPlanet** - *Updating to new MC Versions* - [MyPlayPlanet](https://myplayplanet.net) 

See also the list of [contributors](https://github.com/Postremus/WorldFuscator/contributors) who participated in this project.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details