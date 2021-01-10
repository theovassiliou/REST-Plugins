# Installation

## Source Installation - Recommended

Currently we only support installation from source. This means that use the RJ-Plugin in you TTCN-3 project you have 
to install it in your workspace.


### Step 1 - Clone Repository

There a several ways on how to clone the repository. Assuming that you have git installed on your computer run and you would like to
checkout the github repo at `$(CODING)`. 

```
cd $(CODING)
git clone https://github.com/theovassiliou/REST-Plugins.git
```

This creates you a folder named `REST-Plugins\`.
Within this folder there is the `RJ-Plugins` project folder. This TTworkbench project contains our `RJ-Plugin`. 

### Step 2 - TTworkbench workspace

Start TTworkbench and open a/your workspace. 


### Step 3 - Import REST-Plugins project folder into workspace
In TTworkbench:

Import -> Existing Projects into Workspace
	Select root directory: $(CODING)/REST-Plugins/RJ-Plugin
	
### Step 4 - Initial check your installation in TTworkbench
In TTworkbench-TTCN Development View:

	Open File (double-click): `ttcn3/PetStoreExample.ttcn3`
	Run->Build
	This creates a new folder `clf/` that contains a TTworkbench campaig-loader file.
	Open File (double-click): `clf/PetStoreExample.clf`
	This opens the TTCN-3 Execution Management
	Select and Run a test case by double-clicking

You should something like ![Executed Testcase](./doc/images/ExecutedTestcases.png "Executed Testcases")

### Step 5 - Create own TTCN-3 project
In TTworkbench-TTCN Development View:

	New TTCN-3 Project: TestPetProject
	Next -> Nexta
	Projects Tab: Classpath : Add ... RJ-Plugin
	Copy RJ-Plugin/ttcn3/PetStoreExample.ttcn3 from  to TestPetProject/ttcn3/
	Rename module, filename and module name, e.g. MyExample(.ttcn3)
	Right click on MyExample.ttcn3 -> TTCN-3 Source -> Configure As Main Module
	Right click on TestPetProject project -> Properties -> TTCN-3 - Compiler - TTthree -> Code Generation
		Select: Generate a default test campaign
		Select: Use arbitrary large integer values
		Apply and Close
	Build TTCN-3 module
	Open created campaign loader file
	Confirm "Adapter confiugration file does not exist. Create a default one?" with "Yes"
	Select: "Test Adapter" Tab
	Double-Click: Codecs
		Add ... -> Select REST/JSON Codec
				   Select Encoding: RESTful/json
				   Select CheckMark
		Select Ports Tab
			Port Provider Add ... Select "REST Port"
		    Select CheckMark
			Finish
	Run Test Case

