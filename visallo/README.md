# Pente Game Analytics with Visallo
Visualize and analyze [Pente.org](http://pente.org) games using [Visallo](http://www.visallo.com).

## What is This?
This sub-project uses the [open-source](https://github.com/v5analytics/visallo) version of Visallo to search games and to provide a visual graph showing win/loss relationships between Pente games and players.

## Requirements
Pente Graph is a Java 8 web application which uses Maven for building and running. You will need to install [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Maven 3.3.x](https://maven.apache.org/install.html) to run it on your own computer. The supported web browsers are Chrome, Firefox, and Safari.

## Getting Started

### Run Visallo

1. Build: `mvn package`
1. Run:`./run.sh`
1. Browse: [http://localhost:8080](http://localhost:8080)
1. Login using a username that you choose. No password is needed with this default configuration.

### Import Pente Game Archives

1. Download game archive (.zip) files from the [Pente Game Database](https://pente.org/gameServer/controller/search?quick_start=1). You must be a Pente.org member.
1. Click the Graph icon on the left. Drag and drop the zip files onto the graph. A dialog will appear. Click "Import". Games and players will be imported within a few moments.

### Explore!

1. Click the Find icon on the left. Type "*" in the search box and press Enter. All games and players will appear in the result list. Any results can be dragged to the graph.
1. Use the search filters to refine the search.
1. Click on any result to see game/player details.
1. Results can be dragged to the graph where relationships can be displayed between games and players.
