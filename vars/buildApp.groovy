#!groovy

/**
 * Build a docker image from the current checkout with the specified
 * configuration.
 *
 * Named parameters:
 *
 *    `name` (required): the name of the Docker image to build
 *
 * Usage (e.g. in a pipeline script):
 *
 *     buildApp(name: 'myorg/myapp')
 *
 * The function's job is to encapsulate our conventions for building
 * applications from Dockerfiles. Namely:
 *
 * 1. The application version is returned by `buildMeta.version()` (see
 *    `buildMeta.groovy` in the same directory as this file).
 *
 * 2. The Docker image is tagged with the version number, only with '+'
 *    replaced with '-' (Docker doesn't allow '+' characters in image tags).
 *
 * 3. We use the output of `git archive` as the `docker build` context. This
 *    ensures that the Docker build context can only include checked-in files.
 *
 * This function returns the Docker image object which can be used by
 * subsequent build steps.
 */
def call(Map parameters = [:]) {
    name = parameters.name
    version = buildMeta.version()
    tag = version.replaceAll('\\+', '-')

    if (!name) {
        error 'you must set an image name'
    }

    // Set the build metadata
    currentBuild.displayName = version
    currentBuild.description = "Docker: ${tag}"

    sh "git archive HEAD | docker build -t ${name}:${tag} -"
    return docker.image("${name}:${tag}")
}
