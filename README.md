# nb-project-readme

A NetBeans IDE plugin that injects your project's `README` files right into the
logical **Project Files** view tree.

---

## Features

* **Multi-Format Discovery:** Dynamically scans the root directory for standard
readme files:
* `README.md`
* `README.me`
* `README.txt`
* `README`)

---

## Requirements

* **Java Development Kit (JDK):** Version 11 or higher
* **Apache NetBeans:** Compatible with modern NetBeans distributions (`RELEASE210`+)
* **Apache Maven:** 3.6.0+

---

### Installation & Execution

**1. Clone and compile the codebase:**

```bash
git clone https://github.com/stefanofornari/nb-project-readme.git
cd nb-project-readme
mvn clean install

```

**2. Launch a test development environment:**
To run a clean sandbox instance of NetBeans with the plugin pre-installed, execute:

```bash
mvn nbm:run-ide

```

**3. Manual Distribution:**
After a successful build phase, you can locate the distribute package inside the target folder:

```text
target/nb-project-readme-1.0-SNAPSHOT.nbm

```

To install it into your primary IDE, navigate to `Tools` ➔ `Plugins` ➔ `Downloaded`, select the generated `.nbm` file, and click **Install**.

---

## License

This project is licensed under the Apache License - see the LICENSE file for details.
