FROM sbtscala/scala-sbt:openjdk-8u342_1.8.1_2.12.17

# Set working directory in docker
WORKDIR /app

# Define default command.
ENTRYPOINT [ "/bin/bash", "/app/entrypoint.sh" ]

# Write script entrypoint from (echo multiline)
RUN echo '\n\
    #!/bin/bash\n\
    set -e\n\
    nohup sbt -Dsbt.server.forcestart=true -Dsbt.server.autoconnect=false -Dsbt.supershell=false -Dsbt.log.noformat=true -Dsbt.col &\n\
    bash' > entrypoint.sh

COPY . .

# Install all dependencies
# and run a dry test
RUN sbt update && \
    sbt compile && \
    sbt test:run
