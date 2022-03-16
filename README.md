# WiP

A mod meant to dump recipes from NEI for use outside of the running game

Primarily used for my project WebNei, which needs to clone much NEI content. 

A fork of [Nei Data Dump](https://github.com/NamesAreAPain/NEIDataDump)

# Specs
I recommend allocating at least 12gb of ram, if not 16gb. 

# Usage

`/neidatadump {args}` will dump all data for that argument. Options are `r` for recipes, `g` for GUIs, `i` for items, `a` for aspects (warning, slow), and `o` for oredict

All output should be in a `dumps` folder

GUI export broken on GT Mega until NEI is updated, needs version 2.1.18+