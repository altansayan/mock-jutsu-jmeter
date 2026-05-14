# 🥷 mock-jutsu Implementation Plan

## Phase 1: Python Core Engine
- Initialize `python/` directory as a package.
- Implement `Profile` class for coherent data generation.
- Implement generators for all 40+ data types.
- Add locale JSON files for TR, UK, DE, FR, RU, US.
- CLI interface using `click` or `argparse`.

## Phase 2: JMeter Plugin (Java)
- Set up Maven project in `jmeter-plugin/`.
- Implement `AbstractFunction` class.
- Bridge Java to Python (via Jython or gRPC/REST if standalone, or porting logic to Java). 
- *Decision:* For simplicity and zero-dependency, we might port the core generation logic to Java for the plugin.

## Phase 3: Web UI
- Create single-file HTML/JS app in `web-ui/`.
- Port logic to JavaScript.

## Phase 4: CI/CD & GitHub Pages
- GitHub Actions for testing.
- Automated deployment of Web UI to GitHub Pages.
