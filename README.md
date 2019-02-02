# `WorldFuscator`

Fuscation for everybody and everything.

## What?

Worldfuscator allows to hide blocks from view, by replacing them by a given other material.
This works based on permissions, and, dependeing on which plugin is installed, either the WGK Teams, or on the Worldguard Regions.


## Features

* High Performance fuscation of blocks
* Automatic refresh of fuscated blocks based on BlockRegion or Wargear events
* MC 1.13 compatible
* Fuscates to Ender Stone (NON CHANGEABLE)

## Debug Mode
The debug mode prints information about the fuscation in the console. It also allows to save chunks as .mcp files.
.mcp files contain every information sent by the server about a chunk saved into a file.
This includes:
* Blocks & Block State
* Lighting
* Tile Entities
* Biome

This is enabled either by changing the configuration file, or by wielding the mighty BLAZE_ROD.

## Example Configuration
```
debug:
  enabled: false
hidden-materials:
 - 'minecraft:red_wool
```
