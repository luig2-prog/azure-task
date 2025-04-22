<a name="readme-top"></a>



<div align="center">

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]


[Spanish](#documentación-en-español)
[English](#english-documentation)

### English documentation

# Azure DevOps Task Manager

A Java application for managing tasks in Azure DevOps. This application allows you to create and delete tasks in Azure DevOps by reading task data from a CSV file.

## Features

- Read task data from a CSV file
- Create tasks in Azure DevOps
- Delete tasks from Azure DevOps
- Concurrent processing of tasks for better performance
- Robust error handling and logging

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Azure DevOps account with appropriate permissions
- Personal Access Token (PAT) for Azure DevOps

## CSV File Format

The CSV file should have the following columns in order:

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

Example:

```csv
Title,Description,AssignedTo,IterationPath,AreaPath,OriginalEstimateHours,RemainingHours,ParentStory,Organization,Project,Area,Username,Token
Task 1,Description for task 1,user@example.com,Project\Iteration 1,Project\Area 1,8,8,12345,myorg,myproject,myarea,username,pat
Task 2,Description for task 2,user@example.com,Project\Iteration 1,Project\Area 1,4,4,12345,myorg,myproject,myarea,username,pat
```

## Building the Application

To build the application, run the following command:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory.

## Running the Application

To run the application, use the following command:

```bash
java -jar target/azure-task-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Make sure to place your CSV file named `tasks.csv` in the same directory as the JAR file.

## Configuration

The application reads the CSV file from the current directory. You can modify the file path in the `Main.java` file if needed.

## Logging

The application uses SLF4J with Logback for logging. Log files are created in the current directory.

## Best Practices

- Keep your Personal Access Token secure and never commit it to version control
- Use a CSV file with a header row to ensure proper column mapping
- Validate your CSV data before running the application
- Use appropriate error handling when processing tasks

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

Luis Hernandez Jimenez
- Email: luisjimenezh8@gmail.com
- LinkedIn: [Luis Hernandez Jimenez](https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/)

</div>

## Main Features

- **Creating tasks in Azure by filling out an excel file**: You can create N number of tasks by filling out an excel file and executing the project

<p align="right">(<a href="#readme-top">go back up</a>)</p>

## To initialize

### Prerequisites

- OpenJDK see [official documentation](https://openjdk.org/projects/jdk/22/)

```sh
java -version
```

- Maven see [official documentation](https://maven.apache.org/download.cgi)

```sh
mvn -version
```

### Installation

1. Clone the repository

```sh
git clone https://github.com/luig2-prog/azure-task.git
```

#### Steps in command console

1. Go to the root of the project and run the following command

```sh
mvn clean package
```

2. Fill the information in the task.xlsx file

3. Execute the generated .jar file

```sh
java -jar /target/azure-task-1.0-SNAPSHOT.jar
```


<p align="right">(<a href="#readme-top">go back up</a>)</p>

## Contribute to the project

Contributions are the engine that drives the open source community, by creating and teaching we learn much more. Every contribution you make is immensely valued. If you dare, thank you very much for being part of this journey of collaboration and discovery

Any contributions you make are **greatly appreciated**!

If you have any suggestions or improvements to the project please create a [_fork_](https://github.com/luig2-prog/azure-task/fork) of the repository and create a [_pull request_](https://github. com/luig2-prog/azure-task/pulls). You can also just open a [_issue_](https://github.com/luig2-prog/azure-task/issues) with the tag "enhancement".

Here's a quick guide:

1. Make a [_fork_](https://github.com/luig2-prog/azure-task/fork) of the Project
2. Clone your [_fork_](https://github.com/luig2-prog/azure-task/fork) (`git clone <fork URL>`)
3. Add the original repository as remote (`git remote add upstream <URL of the original repository>`)
4. Create your Feature Branch (`git switch -c feature/CaracteristicaIncreible`)
5. Make your Changes (`git commit -m 'Add: someAwesomeFeature'`)
6. Push the Branch (`git push origin feature/CaracteristicaIncreible`)
7. Open a [_pull request_](https://github.com/luig2-prog/azure-task/pulls)

**Collaborators!**

[![Contribuidores](https://contrib.rocks/image?repo=luig2-prog/azure-task)](https://github.com/luig2-prog/azure-task/graphs/contributors)

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>


## 🛠️ Stack

 [![Java][java-badge]][java-url]

 ![Apache Maven][maven-url]

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>

[astro-url]: https://docs.oracle.com/en/java/

[java-url]: https://tailwindcss.com/
[maven-url]: https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white
[java-badge]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[contributors-shield]: https://img.shields.io/github/contributors/luig2-prog/azure-task.svg?style=for-the-badge
[contributors-url]: https://github.com/luig2-prog/azure-task/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/midudev/la-velada-web-oficial.svg?style=for-the-badge
[forks-url]: https://github.com/luig2-prog/azure-task/network/members
[stars-shield]: https://img.shields.io/github/stars/luig2-prog/azure-task.svg?style=for-the-badge
[stars-url]: https://github.com/luig2-prog/azure-task/stargazers
[issues-shield]: https://img.shields.io/github/issues/midudev/la-velada-web-oficial.svg?style=for-the-badge
[issues-url]: https://github.com/luig2-prog/azure-task/issues
