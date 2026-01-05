# Pac-Man Maze Game (Java)

A Java-based Pac-Man style game built using object-oriented design, graph-based pathfinding, and multiple AI-controlled ghosts.  
The maze is modeled as a graph, and ghost movement is driven by shortest-path algorithms.

---

## Features

- Interactive Pac-Man gameplay
- Graph-based maze representation
- Multiple ghost AI behaviors inspired by the original Pac-Man:
  - **Blinky** – aggressive direct chase
  - **Pinky** – ambush-style targeting
  - **Inky** – predictive movement
  - **Clyde** – hybrid chase/flee behavior
- Pathfinding using a custom priority queue
- Model–View–Controller (MVC) architecture
- Unit tests for core data structures and algorithms
- Built-in user manual

---

## Project Structure

```text
.
├── model/
│   ├── GameModel.java
│   ├── GameBoard.java
│   ├── MazeGraph.java
│   ├── Pathfinding.java
│   ├── MinPQueue.java
│   ├── Blinky.java
│   ├── Pinky.java
│   ├── Inky.java
│   └── Clyde.java
│
├── controller/
│   └── InteractiveGameController.java
│
├── view/
│   └── GameFrame.java
│
├── tests/
│   ├── MazeGraphTest.java
│   ├── MinPQueueTest.java
│   └── PathfindingTest.java
│
├── docs/
│   └── PacMannManual.java
```
## Technologies Used
- Java
- Swing / AWT for GUI rendering
- JUnit 5 for testing
- Custom graph and priority queue implementations

## How to Run

Clone the repository
Open the project in an IDE: IntelliJ IDEA
Run the application: Execute GameFrame.java

