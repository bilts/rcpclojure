# RCP-Clojure

RCP-Clojure is an example Eclipse RCP (Rich Client Platform) product written in Clojure. It is based on the RCP Mail 
template created by PDE.  It acknowledges that Eclipse is currently a poor Clojure editor and provides ant tasks for 
building the RCP plugin and application.

The project's goal is to provide an easy starting point for developers to write Eclipse plugins and products in Clojure.
As such, it does not attempt to be a complete wrapper around the Eclipse platform or even a coherent Clojure UI library.
However, the code contains some examples of how a developer could make Clojure code coexist nicely with RCP and SWT:

- navigation_view.clj produces a TreeViewer control which displays the data contained in a native Clojure map/vector structure
- view.clj produces a complicated layout in a simple, declarative manner
- application.clj concisely adds menus and toolbars

## Setup

Note: Your Eclipse platform version must match the version of your *JRE*.  That is, if you are on a 64-bit OS with a 32-bit
JRE, you need the 32-bit version of Eclipse.

### Summary (32-bit Windows Only)

    cd build
    ant setup product run
Expect the preceding command to take a long time as the product downloads (around 120MB).

### build.properties

Update the following properties in build.properties for your platform:

    baseos=win32
    basews=win32
    basearch=x86

### Automatic platform setup

You may obtain all of the prerequisites through an ant task as follows:

If you are not running on Windows with a 32-bit JRE, set the following properties in build.properties to point to the eclipse
downloads for your platform.  You may find the proper URLs from this site: http://download.eclipse.org/eclipse/downloads/

    platform.url = http://download.eclipse.org/eclipse/downloads/drops/R-3.5.2-201002111343/eclipse-platform-3.5.2-win32.zip
    deltapack.url = http://download.eclipse.org/eclipse/downloads/drops/R-3.5.2-201002111343/eclipse-3.5.2-delta-pack.zip
    
Run `ant setup` in the build directory.  Note that this may take a very long time, as the task fetches and installs the proper
eclipse platforms and plugins.

### Manual

From http://download.eclipse.org/eclipse/downloads/ download the following items for the latest release
- Platform Runtime Binary for your platform
- PDE Runtime Binary
- Deltapack

Unzip the first two items into the same folder, and the last into a separate folder.

Update the following properties in build.properties:
    rcp.eclipse.dir = <Path to Platform Runtime Binary> 
    pde.eclipse.dir = <Path to Platform Runtime Binary>
    deltapack.eclipse.dir = <Path to Deltapack>

## Building

The RCP product and plugin can be built using the following ant tasks in build/build.xml:
- product: Builds the RCP product.  This only needs to be run when new plugin dependencies are added or when the product executable needs to be rebuilt
- deploy: Builds and deploys the product's main plugin.  This runs much faster than the product task.
- run: Runs the product
- clean: Cleans up temporary build files
- dist-clean: Cleans up both temporary build files and distributable builds
 