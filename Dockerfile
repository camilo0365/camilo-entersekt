FROM openjdk

ENV DEBIAN_FRONTEND=noninteractive

# Variables for paths inside the image
ARG BIN_DIR=/usr/local/bin
ARG APP_DIR=/app


#===================================================================================================
#                                      ADD NECESSARY BINARIES
#
# * Tini: Small init (PID 1) process to handle signals and zombie reaping properly. Java programs
#   terminated with SIGTERM or SIGINT exit with codes 143 and 130 respectively. This could be an
#   issue when docker tries to terminate the process smoothly. Tini will handle those signals
#   instead. [1][2][3][4]
#===================================================================================================
WORKDIR $BIN_DIR
ARG TINI_VERSION=0.16.1

RUN set -ex \
    && buildDeps=' \
            wget \
        ' \
	&& apt-get update && apt-get install -y $buildDeps maven --no-install-recommends && rm -rf /var/lib/apt/lists/* \
	&& wget -O tini "https://github.com/krallin/tini/releases/download/v${TINI_VERSION}/tini" \
	&& chmod +x * \
	&& apt-get purge -y --auto-remove $buildDeps


WORKDIR $APP_DIR

# Get maven dependencies
COPY pom.xml .
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Build artifacts
ADD src $APP_DIR/src
RUN ["mvn", "package"]

# Execution
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/tini", "-vv", "--"]
CMD ["java", "-classpath" , "target/dependency/*:target/camilo-submission-1.0.jar", "com.camilo.assessment.Main"]


#===================================================================================================
#                                        REFERENCES
#
# [1] https://medium.com/@gchudnov/trapping-signals-in-docker-containers-7a57fdda7d86
# [2] https://blog.phusion.nl/2015/01/20/docker-and-the-pid-1-zombie-reaping-problem/
# [3] https://medium.com/@nagarwal/an-init-system-inside-the-docker-container-3821ee233f4b
# [4] https://github.com/krallin/tini
#===================================================================================================