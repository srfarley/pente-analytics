# Pente Graph [![Build Status](https://travis-ci.org/srfarley/pente-graph.svg?branch=master)](https://travis-ci.org/srfarley/pente-graph)
Visualize and analyze [Pente.org](http://pente.org) games using [Visallo](http://www.visallo.com).

## What is This?
This project uses the [open-source](https://github.com/v5analytics/visallo) version of Visallo to search games and provide a visual graph showing win/loss relationships between Pente games and players.


## Getting Started
1. Build: `mvn package`
1. Run:`./run.sh`
1. Browse: [http://localhost:8080](http://localhost:8080)
1. Login using a username that you choose. No password is needed with this default configuration.
1. Download game archive (.zip) files from the [Pente Game Database](https://pente.org/gameServer/controller/search?quick_start=1). You must be a Pente.org member.
1. Click the Graph icon on the left. Drag and drop the zip files onto the graph. A dialog will appear. Click "Import". Games and players will be imported within a few moments.
1. Click the Find icon on the left. Type "*" in the search box and press Enter. All games and players will appear in the result list. Any results can be dragged to the graph.
1. Use the search filters to refine the search.
