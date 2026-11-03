# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Jenkins Shared Library** - a reusable library of pipeline steps and utilities that can be used across Jenkins pipelines. Jenkins automatically loads libraries from the `vars/` directory and makes them available to all pipelines in the Jenkins instance.

## Directory Structure

- **`vars/`** — Jenkins shared library steps and utilities
  - Each `.groovy` file in `vars/` becomes a callable step (e.g., `vars/easyPipeline.groovy` becomes `easyPipeline()`)
  - Each step should have a corresponding `.txt` file with documentation/help text
  - Steps follow the Jenkins shared library pattern: they must have a `call()` method
  - This directory contains ~50 pipeline steps for various CI/CD tasks (build, deploy, notifications, etc.)

- **`src/`** — Compiled Groovy classes (optional, for more complex logic)
  - Contains `AddSidebarLinkAction.groovy`: a Jenkins Action class that adds sidebar links to build pages
  - Classes here can be imported and used by steps in `vars/`

- **`resources/`** — Static resources accessed by pipeline steps
  - `com/planetpope/scripts/` — Shell scripts (Linux/Windows) for cross-platform execution
  - `com/planetpope/emailtemplate/` — HTML email templates for build notifications
  - `com/planetpope/maven-settings/` — Maven configuration files (Artifactory settings)
  - `com/planetpope/api/` — API request templates (JSON payloads for external APIs like Jira)

## Common Development Tasks

### Working with Shared Library Steps

When creating or modifying a step in `vars/`:

1. **Create/Edit a Step** — Add or modify a `.groovy` file in `vars/`
   - Must contain a `call()` method as the entry point
   - Can accept parameters: `call(String param1, Map config)`, `call(body)`, etc.
   - Steps can call other steps, use Jenkins objects (`currentBuild`, `env`), and execute shell commands

2. **Add Documentation** — Create a `.txt` file with the same name
   - This is displayed as help text when users view the step in Jenkins UI
   - Format: Plain text description of what the step does and its parameters

3. **Example Step Structure**:
   ```groovy
   // vars/myStep.groovy
   def call(Map config = [:]) {
       echo "Running my step with config: ${config}"
       sh "some-command"
   }
   ```

### Common Step Patterns

- **Closure-based DSL**: Some steps accept a closure body that users configure (see `easyPipeline.groovy`)
- **Map-based config**: Steps accept a map of parameters for simpler configuration (see `addSidebarLink.groovy`)
- **Hybrid approach**: Mix both patterns for flexibility

### Using Resources

Resources are accessed using Jenkins' `libraryResource()` function:
```groovy
def template = libraryResource('com/planetpope/emailtemplate/build-results.html')
def apiPayload = libraryResource('com/planetpope/api/jira/createIssue.json')
def script = libraryResource('com/planetpope/scripts/linux/hello-world.sh')
```

## Architecture Notes

### Jenkins Shared Library Convention

- Library loads from `vars/` automatically when Jenkins loads the shared library
- Classes in `src/` follow standard Groovy package structure under `com/planetpope/`
- This repository name (`github-api-global-lib`) becomes the library name in Jenkins

### Shared Library Integration

When this library is configured in Jenkins:
1. Users reference it in their Jenkinsfile: `@Library('github-api-global-lib') _`
2. All steps in `vars/` become callable: `easyPipeline()`, `addSidebarLink()`, etc.
3. Classes from `src/` can be imported if needed
4. Resources can be accessed via `libraryResource()` using their path from `resources/`

### Design Pattern Notes

- Steps are designed to be composable — they can call other steps
- The `easyPipeline` step is a high-level orchestrator that chains together common build stages
- Steps like `addSidebarLink` and `deployApplication` wrap Jenkins/external tool integrations
- Email templates and API payloads use template variables (e.g., `$key`, `$summary`) that are substituted at runtime

## File Format Conventions

- **Groovy files**: Follow Jenkins shared library conventions; should be valid Groovy code
- **Documentation files**: Plain text (`.txt`) with human-readable descriptions
- **Resource templates**: JSON and shell scripts; use `$variable` syntax for substitution
- **Properties files**: Standard Java properties format (key=value)

## Typical Workflow

1. **Before making changes**, understand which steps (in `vars/`) call each other to avoid breaking dependencies
2. **When adding a new step**, ensure you add both `.groovy` and `.txt` files
3. **Testing in Jenkins**: Commit to a branch and reference it in Jenkins library configuration to test
4. **Resource updates**: When modifying email templates or API payloads, verify the variable substitution works with calling steps

## Git Workflow

- Commit messages are typically descriptive of what changed (recent history shows experimental iterations)
- `.dccache` is excluded in `.gitignore` (cache from editor integration)
- Standard Java/IDE ignores (`.class`, `.idea/`, JAR files) are also excluded
