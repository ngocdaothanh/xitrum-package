For tips and tricks, see the presentation
`Effective SBT <http://jsuereth.com/scala/2013/06/11/effective-sbt.html>`_
by Josh Suereth.

Publish to local
----------------

While developing, you may need do local publish. Run
``sbt publishLocal``.

To delete the local publish:

::

  $ find ~/.ivy2 -name *xitrum-package* -delete

Publish to Sonatype
-------------------

See:
https://github.com/sbt/sbt.github.com/blob/gen-master/src/jekyll/using_sonatype.md

Create file ~/.sbt/1.0/sonatype.sbt:

::

  credentials += Credentials("Sonatype Nexus Repository Manager",
                             "oss.sonatype.org",
                             "<your username>",
                             "<your password>")

Then:

1. Copy content of
     dev/build.sbt.end   to the end of build.sbt
     dev/plugins.sbt.end to the end of project/plugins.sbt
2. Run ``sbt publishSigned``.
3. Login at https://oss.sonatype.org/ and from "Staging Repositories" select the
   newly published item, click "Close" then "Release".
