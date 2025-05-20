<a name="readme-top"></a>

<div align="center">

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]

# Azure DevOps Task Manager

A Java application for managing tasks in Azure DevOps. This application allows you to create and delete tasks in Azure DevOps by reading task data from an Excel or CSV file.

[English](#english-documentation) | [Spanish](#documentación-en-español)

</div>

<a id="english-documentation"></a>

## English Documentation

## Overview

Azure DevOps Task Manager is a tool designed to simplify the creation and management of tasks in Azure DevOps projects. By providing task details in a spreadsheet format, you can automate the creation of multiple tasks at once, saving time and reducing manual effort.

## Key Features

- Read task data from Excel or CSV files
- Create multiple tasks in Azure DevOps simultaneously
- Delete tasks from Azure DevOps
- Concurrent processing for better performance
- Robust error handling and logging

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Azure DevOps account with appropriate permissions
- Personal Access Token (PAT) for Azure DevOps

## Getting Started

### Prerequisites

- OpenJDK - [official documentation](https://openjdk.org/projects/jdk/)
  ```sh
  java -version
  ```

- Maven - [official documentation](https://maven.apache.org/download.cgi)
  ```sh
  mvn -version
  ```

### Installation

1. Clone the repository
   ```sh
   git clone https://github.com/luig2-prog/azure-task.git
   ```

2. Navigate to the project directory
   ```sh
   cd azure-task
   ```

3. Build the project
   ```sh
   mvn clean package
   ```

### Usage

1. Prepare your task data in the Excel file (`tasks.xlsx`) or CSV file (`tasks.csv`)

2. Run the application:
   ```sh
   java -jar target/azure-task-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Input File Format

### Excel File Format

The Excel file (`tasks.xlsx`) should have the following columns in order:

1. Title
2. Description
3. AssignedTo
4. IterationPath
5. AreaPath
6. OriginalEstimateHours
7. RemainingHours
8. ParentStory
9. Organization
10. Project
11. Area
12. Username
13. Token

### CSV File Format

Alternatively, you can use a CSV file with the same columns:

```csv
Title,Description,AssignedTo,IterationPath,AreaPath,OriginalEstimateHours,RemainingHours,ParentStory,Organization,Project,Area,Username,Token
Task 1,Description for task 1,user@example.com,Project\Iteration 1,Project\Area 1,8,8,12345,myorg,myproject,myarea,username,pat
Task 2,Description for task 2,user@example.com,Project\Iteration 1,Project\Area 1,4,4,12345,myorg,myproject,myarea,username,pat
```

## Configuration

The application reads the input file from the current directory. You can modify the file path in the `Main.java` file if needed.

## Logging

The application uses SLF4J with Logback for logging. Log files are created in the current directory.

## Best Practices

- Keep your Personal Access Token secure and never commit it to version control
- Use input files with header rows to ensure proper column mapping
- Validate your data before running the application
- Use appropriate error handling when processing tasks

## 🛠️ Technology Stack

[![Java][java-badge]][java-url]
![Apache Maven][maven-url]

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have suggestions for improving the project, please fork the repository and create a pull request, or open an issue with the tag "enhancement".

Here's how to contribute:

1. Fork the Project
2. Clone your fork (`git clone <fork URL>`)
3. Add the original repository as remote (`git remote add upstream https://github.com/luig2-prog/azure-task.git`)
4. Create your Feature Branch (`git switch -c feature/AmazingFeature`)
5. Make your Changes (`git commit -m 'Add: some amazing feature'`)
6. Push the Branch (`git push origin feature/AmazingFeature`)
7. Open a Pull Request

### Contributors

[![Contributors](https://contrib.rocks/image?repo=luig2-prog/azure-task)](https://github.com/luig2-prog/azure-task/graphs/contributors)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Author

Luis Hernandez Jimenez
- Email: luisjimenezh8@gmail.com
- LinkedIn: [Luis Hernandez Jimenez](https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

<a id="documentación-en-español"></a>

## Documentación en Español

*Esta sección está pendiente de traducción completa. Por favor, consulte la documentación en inglés mientras tanto.*

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
[java-url]: https://docs.oracle.com/en/java/
[maven-url]: https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white
[java-badge]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[contributors-shield]: https://img.shields.io/github/contributors/luig2-prog/azure-task.svg?style=for-the-badge
[contributors-url]: https://github.com/luig2-prog/azure-task/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/luig2-prog/azure-task.svg?style=for-the-badge
[forks-url]: https://github.com/luig2-prog/azure-task/network/members
[stars-shield]: https://img.shields.io/github/stars/luig2-prog/azure-task.svg?style=for-the-badge
[stars-url]: https://github.com/luig2-prog/azure-task/stargazers
[issues-shield]: https://img.shields.io/github/issues/luig2-prog/azure-task.svg?style=for-the-badge
[issues-url]: https://github.com/luig2-prog/azure-task/issues
[stars-url]: https://github.com/luig2-prog/azure-task/stargazers
[issues-shield]: https://img.shields.io/github/issues/midudev/la-velada-web-oficial.svg?style=for-the-badge
[issues-url]: https://github.com/luig2-prog/azure-task/issues
