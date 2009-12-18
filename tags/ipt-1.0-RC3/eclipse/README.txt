Eclipse 3.X instructions

These instructions are intended for contributors to the IPT source
code repository that want to run the Eclipse IDE. It describes how to
configure Eclipse for the correct coding styles. 

This documentation is largely based on the GWT project.

== Configure Eclipse Environment==

---------- Required IPT variables ---------

Window->Preferences->General->Workspace->Linked Resources
Create a variable named "M2_REPOS" pointing to your local
Maven repository (i.e. ~/.m2/repository).

---------------- Spelling -----------------

Window->Preferences->General->Editors->Text Editors->Spelling
Enable spell checking
Use "eclipse/settings/english.dictionary".

------------ Output Filtering -------------

Window->Preferences->Java->Compiler->Building
Make sure "Filtered Resources" includes ".svn/"

---------- Code style/formatting ----------

Window->Preferences->Java->Code Style->Formatter->Import...
  eclipse/settings/code-style/ipt-format.xml

----------- Import organization -----------

Window->Preferences->Java->Code Style->Organize Imports->Import...
  eclipse/settings/code-style/ipt.importorder

------------ Member sort order ------------

Window->Preferences->Java->Appearance->Members Sort Order
There is no import here, so make your settings match:
  settings/code-style/ipt-sort-order.png

First, members should be sorted by category.
1) Types
2) Static Fields
3) Static Initializers
4) Static Methods
5) Fields
6) Initializers
7) Constructors
8) Methods

Second, members in the same category should be sorted by visibility.
1) Public
2) Protected
3) Default
4) Private

Third, within a category/visibility combination, members should be sorted
alphabetically.
 
------------ Compiler settings ------------
Window->Preferences->Java->Compiler
Set the compiler compliance level to 1.5.

------- Compiler errors & warnings --------
Window->Preferences->Java->Compiler->Errors/Warnings

The following warnings are suggested.

Code Style:
- Method with a constructor name

Potential programming problems:
- Assignment has no effect
- Accidental boolean assignment
- 'finally' does not complete normally
- Using a char array in string concatentation
- Hidden catch block
- Inexact type match for vararg arguments

Name shadowing and conflicts: all except "Local variable" hiding

Deprecated and restricted API: all

Unnecessary code: all except "Unnecessary 'else' statement"

Generic types: all except "Generic type parameter declared with final type bound"

Annotations:
- Annotation is used as super interface
- Enable @SuppressWarnings annotations

== Checkstyle ==

Checkstyle is used to enforce good programming style.

1. Install Checkstyle version 4.4.2 (newer versions will not work)

2. Enable Custom IPT Checkstyle checks using the GWT customchecks jar:

Copy "settings/code-style/gwt-customchecks.jar" into:
  <eclipse>/plugins/com.atlassw.tools.eclipse.checkstyle_x.x.x/extension-libraries

Restart Eclipse.
("gwt-customchecks.jar" is also built from source into build/lib during a full
 build)

3. Import IPT Checks:

Window->Preferences->Checkstyle->New...
Set the Type to "External Configuration File"
Set the Name to "IPT Checks" (important)
Set the location to "settings/code-style/ipt-checkstyle.xml".
Suggested: Check "Protect Checkstyle configuration file".
Click "Ok".

4. Import IPT Checks for Tests

Repeat step 2, except:
Set the Name to "IPT Checks for Tests" (important)
Set the location to "settings/code-style/ipt-checkstyle-tests.xml".

