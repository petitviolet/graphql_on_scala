run:
	sbt 'project main' '~run' -mem 2048 -jvm-debug 5005

image/build:
	sbt 'project main' 'docker:publishLocal'
