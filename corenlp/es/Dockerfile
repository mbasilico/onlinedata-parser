FROM openjdk:8

ENV CORENLP_ARCHIVE_VERSION=2018-10-05
ENV CORENLP_ARCHIVE=stanford-corenlp-full-${CORENLP_ARCHIVE_VERSION}.zip \
  CORENLP_PATH=/corenlp

RUN wget http://nlp.stanford.edu/software/$CORENLP_ARCHIVE \
  && unzip $CORENLP_ARCHIVE \
  && mv $(basename ../$CORENLP_ARCHIVE .zip) $CORENLP_PATH \
  && rm $CORENLP_ARCHIVE

COPY CoreNLP-spanish.properties $CORENLP_PATH
WORKDIR $CORENLP_PATH

RUN wget http://nlp.stanford.edu/software/stanford-spanish-corenlp-${CORENLP_ARCHIVE_VERSION}-models.jar

RUN export CLASSPATH="`find . -name '*.jar'`"

CMD /usr/bin/java -mx512m -cp "*" edu.stanford.nlp.pipeline.StanfordCoreNLPServer -serverProperties CoreNLP-spanish.properties -port $PORT -username socialaryfilter -password socialaryfilter2019