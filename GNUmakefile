# $Format: "VERSION=$JavashRelease$"$
VERSION=0.001
JAR=../javash-$(VERSION).jar
java:=$(shell find com/sig/javash -name '*.java' -print)
class:=$(java:%.java=%.class)
INSTALL_CLASS=/sig/share/site-java/classes
INSTALL_DOC=/sig/share/site-java/doc/javash

all: rekey $(class) doc

rekey:
	@+-prcs rekey -q -f .. ../javash

test: rekey $(class)
	CLASSPATH=.:$$CLASSPATH java com.sig.javash.Main

%.class: %.java
	javac -g -deprecation $<

install: all doc
	find com/sig/javash -name '*.class' -exec cp -v {} $(INSTALL_CLASS)/{} \;
	rm -rfv $(INSTALL_DOC)
	cp -av doc $(INSTALL_DOC)

dist: rekey $(JAR)

$(JAR): $(class) doc
	jar cvf $@ *

doc: doc/packages.html
doc/packages.html: $(java)
	@mkdir -p doc
	javadoc -author -version -d doc $(java)

clean:
	find com/sig/javash -name '*.class' -exec rm -fv {} \;
	rm -rfv doc

.PHONY: all rekey test install dist clean doc

# $Format: "# $JavashCopyright$"$
# Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
