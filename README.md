# Epidemic Simulation Project

## Overview
The Epidemic Simulation project models the spread of an infectious disease through a population of agents. The simulation takes into account various factors such as infection probability, recovery, mortality, social distancing (confined agents), and mask-wearing. Agents interact in a grid-based environment, and the simulation tracks the epidemic's dynamics over time.

This agent-based model is built using the MAQIT simulator framework and simulates government-imposed policies like wearing masks, confinement rules, and media influence on agent behavior.

## Features
- **Agent-based simulation**: Agents can be in one of several health states: Non-Infected, Infected (Stage 1 and Stage 2), Recovered, or Deceased.
- **Infection dynamics**: Infection spreads based on close contact between agents, with probabilities for infection, recovery, and death.
- **Government policies**: The simulation can model government policies like mask mandates and confinement rules, affecting agent behavior and infection rates.
- **Data logging**: The simulation outputs metrics like the number of infected, recovered, deceased, confined, and mask-wearing agents to a CSV file for further analysis.
- **Customization**: The simulation can be customized with parameters such as the number of agents, simulation steps, infection rates, and other probabilities.

## Key Classes

### `EpidemicSimulation`
This is the main class controlling the simulation. It handles environment setup, robot creation, agent state updates, and data logging. Key methods include:
- **createEnvironment()**: Initializes the simulation environment.
- **createRobot()**: Creates agents (robots), with some agents starting as infected.
- **schedule()**: Main loop of the simulation, updating agent states and logging metrics.
- **simulateHealthOutcome()**: Simulates the health outcome of each agent based on their interactions with others.

### `EpidemicAgent`
This class represents an individual agent in the simulation. Each agent has a health state (Non-Infected, Infected, Recovered, Deceased) and can interact with other agents based on infection probabilities. Agents also decide whether to wear masks or self-confine based on various factors. Key methods include:
- **decideToWearMask()**: Decides whether an agent should wear a mask, based on several factors like media influence and confinement status.
- **decideToConfine()**: Determines whether an agent should self-confine, based on their risk perception and exposure.
- **identifyCloseContacts()**: Identifies whether an agent is in close contact with others and determines infection probability.


## Setup

### Prerequisites
Ensure you have the following:
- Java 8 or higher
- The MAQIT simulator framework

### Installation
1. Clone the repository:
   ```bash
   git clone <repository_url>
