# DRAFT IN PROGRESS

The ForgeRock Doc Maven Plugin builds ForgeRock core documentation
from DocBook XML sources.

This README describes what the plugin expects of core documentation sources,
and shows how to use some of its key features.

The ForgeRock Doc Maven Plugin relies on default branding and common content.
The README shows how to use alternative branding and common content.


# About the ForgeRock Doc Maven Plugin

This Maven plugin centralizes configuration of core documentation,
to ensure that documents are formatted and laid out uniformly.

_This document covers functionality present in 3.0.0-SNAPSHOT._

The project runs multiple plugin executions:

1.  A `pre-site` phase `build` goal to format documents (HTML, PDF, etc.)
2.  A `site` phase `site` goal to lay out documents in site format
2.  A `site` phase `release` goal to lay out documents in release format

With centralized configuration handled by this Maven plugin,
the overall configuration requires at least these arguments:

*   `<projectName>`: OpenAM, OpenDJ, OpenICF, OpenIDM, OpenIG, and so forth
*   `<projectVersion>`: the version, such as `1.0.0-SNAPSHOT`, or `3.3.1`
*   `<releaseVersion>`: the release version, such as `1.0.0`, or `3.3.1`
*   `<googleAnalyticsId>`: to add Google Analytics JavaScript to HTML output

Other features are described in this README.


## Plugin Configuration

You call the plugin from your `pom.xml` as in the following example.

    <build>
     <plugins>
      <plugin>
       <groupId>org.forgerock.commons</groupId>
       <artifactId>forgerock-doc-maven-plugin</artifactId>
       <version>${forgerockDocPluginVersion}</version>
       <inherited>false</inherited>
       <configuration>
        <projectName>MyProject</projectName>
        <projectVersion>1.0.0-SNAPSHOT</projectVersion>
        <releaseVersion>1.0.0</releaseVersion>
        <googleAnalyticsId>${googleAnalyticsId}</googleAnalyticsId>
       </configuration>
       <executions>
        <execution>
         <id>build-doc</id>
         <phase>pre-site</phase>
         <goals>
          <goal>build</goal>
         </goals>
        </execution>
        <execution>
         <id>layout-site</id>
         <phase>site</phase>
         <goals>
          <goal>site</goal>
         </goals>
        </execution>
        <execution>
         <id>layout-release</id>
         <phase>site</phase>
         <goals>
          <goal>release</goal>
         </goals>
        </execution>
       </executions>
      </plugin>
     </plugins>
    </build>

In the example above, `<inherited>false</inherited>` means
only use this plugin configuration for this project or module,
not recursively for all modules that are children of this project.

Unless you want to run the plugin recursively, set `<inherited>false</inherited>`.

## Plugin Output

When you run the `pre-site` phase `build` goal,
the plugin builds the HTML, PDF, etc.,
which you find under `${project.build.directory}/docbkx`,
which is usually `target/docbkx`.

When you run the `site` phase `site` goal,
the plugin copies the documents it built during the `pre-site` phase
under `${project.build.directory}/site/doc` as expected for a Maven project site.

The plugin adds an `index.html` in the `site/doc` directory.
That `index.html` file redirects browsers
to `http://project.forgerock.org/docs.html`,
so be sure to add a `docs.html` to your Maven site.

When you run the `site` phase `release` goal,
the plugin lays out copies of HTML and PDF
under `${project.build.directory}/release/releaseVersion`,
where `releaseVersion` is the release version set in the configuration.

The files in `release/releaseVersion` are those suitable for publication.


## Source Layout Requirements

The plugin assumes that all of your DocBook XML documents
are found under `src/main/docbkx/`.

Documents are expected to be found in folders under that path,
where the folder name is a lowercase version of the document name,
such as `release-notes`, `admin-guide`, `reference`, or similar.

Furthermore, all documents have the same file name
for the file containing the top-level document element, by default `index.xml`.

The plugin expects all images in an `images` folder inside the document folder.

An example project layout looks like this:

     src/main/docbkx/
      legal.xml
      admin-guide/
       images/
       index.xml
       ...other files...
      dev-guide/
       images/
       index.xml
       ...other files...
      install-guide/
       images/
       index.xml
       ...other files...
      reference/
       images/
       index.xml
       ...other files...
      release-notes/
       images/
       index.xml
       ...other files...
      shared/
       sec-accessing-doc-online.xml
       sec-formatting-conventions.xml
       sec-interface-stability.xml
       sec-joining-the-community.xml
       sec-release-levels.xml
       ...other files...

During the build,
the plugin makes a copy of the original sources under the build directory.
It then works on the copy, rather than the original.


# ForgeRock Doc Maven Plugin Features

This section explains how to use key plugin features.


## Using Variables in Documents

The `pre-site` phase `build` goal applies the Maven resource filtering.
 
This means you can use Maven properties to add variables to your documentation.

For example, say you have a property defined in your POM:

    <properties>
      <myUrl>http://docs.forgerock.org/</myUrl>
      ...
    </properties>

During the `pre-site` phase `build` goal,
the plugin replaces `${myUrl}`
in the build copy of the documents with `http://docs.forgerock.org`.

This allows you to use Maven properties in attribute values,
such as `xlink:href="${myURL}"`,
whereas the form using a processing instruction,
`xlink:href="<?eval ${myURL}"?>`,
is not valid XML.


## Building Release Documentation

The `site` phase `release` goal prepares documents for publication.

You might choose to include a release profile in your project.
Or you might choose to call the release goal by hand.

When you call the `release` goal, be sure to
set the appropriate dates, turn off draft mode, and override the Google Analytics ID
as shown in the following example.

     mvn \
     -DisDraftMode=no  \
     -D"gaId=UA-23412190-14" \
     -D"releaseDate=Software release date: January 1, 1970" \
     -D"pubDate=Publication date: December 31, 1969" \
     -DbuildReleaseZip=true \
     clean site org.forgerock.commons:forgerock-doc-maven-plugin:release

If the plugin configuration is not inherited,
then also set `-N` (`--non-recursive`) for the release goal.
Run the `site` goal separately if it must be recursive
(because you build Javadoc during the `site` goal for example).

Appropriate dates should be included in the documents to publish.
* The `releaseDate` indicates the date the software was released.
* The `pubDate` indicates the date you published the documentation.

To build a .zip of the release documentation,
you can set `-DbuildReleaseZip=true` as shown in the above example.
The file, `projectName-releaseVersion-docs.zip`,
can be found after the build in the project build directory.
When unzipped, it unpacks the documentation for the release
under `projectName/releaseVersion/`.


## Generating Single-Chapter Output

By default, the plugin generates output for each document
whose root is named `index.xml`.

You can change this by setting `documentSrcName` when you run Maven.

The following example produces pre-site output
only for a chapter named `chap-one.xml`.

    mvn -DdocumentSrcName=chap-one.xml clean pre-site


## Including & Excluding Output Formats

If you want only one type of output, then specify that using `include`.

The following command generates only PDF output for your single chapter.

    mvn -DdocumentSrcName=chap-one.xml -Dinclude=pdf clean pre-site

Formats include `epub`, `html`, `man`, `pdf`, `rtf`, and `webhelp`.

To exclude formats from the build,
you can use the optional `<excludes>` configuration element.

The following example excludes all formats but HTML from the build.

     <excludes>
      <exclude>epub</exclude>
      <exclude>man</exclude>
      <exclude>pdf</exclude>
      <exclude>rtf</exclude>
      <exclude>webhelp</exclude>
     </excludes>


## Checking Links

By default, the plugin checks links found in the DocBook XML source,
including Olinks.

You can find errors in `${project.build.directory}/docbkx/linktester.err`
after the `site` phase `site` goal runs successfully.

If you want to skip the checks for external URLs,
pass `-DskipLinkCheck=true` to Maven, as in the following example:

    mvn -DskipLinkCheck=true clean site

This capability is provided by Peter Major's
[linktester](https://github.com/aldaris/docbook-linktester) plugin.


## Handling PNG Images

Getting screenshots and other images to look okay in PDF can be a hassle.
The plugin therefore adjusts images to make large PNG images fit in the page,
and adjusts dots-per-inch on PNG images to make them look okay in print.

**Note: Do capture screenshots at 72 DPI. Retina displays can default to 144.**


## Using Syntax Highlighting in HTML

Uses [SyntaxHighlighter](http://alexgorbatchev.com/SyntaxHighlighter/),
rather than DocBook's syntax highlighting capabilities for HTML output.

The highlighting operates only inside `<programlisting>`.

     Source         SyntaxHighlighter   Brush Name
     ---            ---                 ---
     aci            aci                 shBrushAci.js
     csv            csv                 shBrushCsv.js
     html           html                shBrushXml.js
     http           http                shBrushHttp.js
     ini            ini                 shBrushProperties.js
     java           java                shBrushJava.js
     javascript     javascript          shBrushJScript.js
     ldif           ldif                shBrushLDIF.js
     none           plain               shBrushPlain.js
     shell          shell               shBrushBash.js
     xml            xml                 shBrushXml.js

Brush support for `aci`, `csv`, `http`, `ini`, and `ldif` is provided by
[a fork of SyntaxHighlighter](https://github.com/markcraig/SyntaxHighlighter).

To set the language for syntax highlighting use the `language` attribute
on the `<programlisting>` element, as in the following example:

    <programlisting language="java">
    class Test {
        public static void main(String [] args)  {
            System.out.println("This is a program listing.");
        }
    }
    </programlisting>


## Citing Java Code

[JCite](http://arrenbrecht.ch/jcite/) lets you cite, rather than copy and paste,
Java source code in your documentation,
reducing the likelihood that the developer examples get out of sync
with your documentation.

Code citations should fit inside ProgramListing elements with language set
to `java` to pick up syntax highlighting. Use plain citations as in the
following example:

    <programlisting language="java"
    >[jcp:org.forgerock.doc.jcite.test.Test:--- mainMethod]</programlisting>

See the test project,
[forgerock-doc-maven-plugin-test](https://github.com/markcraig/forgerock-doc-maven-plugin-test)
for an example.


## Resolving OLinks in Shared Content

You can now use `xlink:href="CURRENT.DOCID#linkend"` in your links.
The doc build plugin replaces `CURRENT.DOCID` with the current document name.


## Adding Hard Page Breaks in PDF and RTF

You can use the processing instruction `<?hard-pagebreak?>`
to force an unconditional page break in the PDF (and RTF) output.

This processing instruction cannot be used inline,
but instead must be used between block elements.


## Developing UML Diagrams

For UML diagrams, use PlantUML text files instead of images.

[PlantUML](http://plantuml.sourceforge.net/)
is an open source tool written in Java to create UML diagrams from text files.

Using UML instead of drawing images can be particularly useful
when constructing sequence diagrams.

The text files are rendered as PNG images where they are found,
so put your PlantUML `.txt` files in the `images/` directory for your book.
Then reference the `.png` version as if it existed already.

    <mediaobject xml:id="figure-my-sequence-diagram">
     <alt>Generated sequence diagram</alt>
     <imageobject>
      <imagedata fileref="images/my-sequence-diagram.png" format="PNG" />
     </imageobject>
     <textobject>
      <para>
       The sequence diagram is described in images/my-sequence-diagram.txt.
      </para>
     </textobject>
    </mediaobject>

Your PlantUML text files must have extension `.txt`.

To check your images during development, generate them with PlantUML by hand.

    java -jar plantuml.jar image.txt


# Alternative Branding & Common Content

By default, the ForgeRock doc Maven plugin
uses default ForgeRock branding
and pulls in common ForgeRock shared content.

You can use alternative branding and common content.


## Handling Branding

The plugin relies on a branding module that lets you configure alternatives
as in the following example, taken from the top-level plugin configuration.

If you want _different_ branding from the default,
you can use a different version,
or create your own Maven artifact, and include it in the configuration.

The following example shows the full configuration to use the 2.1.3 version.

    <configuration>
      ...
      <brandingGroupId>org.forgerock.commons</brandingGroupId>
      <brandingArtifactId>forgerock-doc-default-branding</brandingArtifactId>
      <brandingVersion>2.1.3</brandingVersion>
      ...
    </configuration>

If you need to create your own branding artifact,
see the `forgerock-doc-default-branding` project as an example.


## Handling Common Content

By default, the plugin replaces the following common files
in the build copy of the sources,
ensuring your documentation includes the latest versions.

    legal.xml
    shared/sec-accessing-doc-online.xml
    shared/sec-formatting-conventions.xml
    shared/sec-interface-stability.xml
    shared/sec-joining-the-community.xml
    shared/sec-release-levels.xml

The plugin does not replace your copies of the files in the original source.

The plugin does not check that you store the files in the expected location.

To avoid using ForgeRock common content, do not reference these files.

If you want _different_ common content from the default,
you can use a different version,
or create your own Maven artifact, and include it in the configuration.

The following example shows the full configuration to use the 2.1.3 version.

    <configuration>
      ...
      <commonContentGroupId>org.forgerock.commons</commonContentGroupId>
      <commonContentArtifactId>forgerock-doc-common-content</commonContentArtifactId>
      <commonContentVersion>2.1.3</commonContentVersion>
      ...
    </configuration>

If you need to create your own shared content artifact,
see the `forgerock-doc-common-content` project as an example.


* * *

This work is licensed under the Creative Commons
Attribution-NonCommercial-NoDerivs 3.0 Unported License.
To view a copy of this license, visit
<http://creativecommons.org/licenses/by-nc-nd/3.0/>
or send a letter to Creative Commons, 444 Castro Street,
Suite 900, Mountain View, California, 94041, USA.

Copyright 2012-2014 ForgeRock AS