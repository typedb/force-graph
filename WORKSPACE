#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

workspace(name = "vaticle_force_layout")

################################
# Load @typedb_dependencies #
################################

load("//dependencies/vaticle:repositories.bzl", "typedb_dependencies")
typedb_dependencies()

# Load //builder/bazel for RBE
load("@typedb_dependencies//builder/bazel:deps.bzl", "bazel_toolchain")
bazel_toolchain()

# Load //builder/java
load("@typedb_dependencies//builder/java:deps.bzl", "rules_jvm_external")
rules_jvm_external()

# Load //builder/kotlin
load("@typedb_dependencies//builder/kotlin:deps.bzl", "io_bazel_rules_kotlin")
io_bazel_rules_kotlin()
load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")
kotlin_repositories()
load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")
kt_register_toolchains()

# Load //builder/python
load("@typedb_dependencies//builder/python:deps.bzl", "rules_python")
rules_python()

# Load //builder/antlr
load("@typedb_dependencies//builder/antlr:deps.bzl", "rules_antlr", "antlr_version")
rules_antlr()

load("@rules_antlr//antlr:lang.bzl", "JAVA")
load("@rules_antlr//antlr:repositories.bzl", "rules_antlr_dependencies")
rules_antlr_dependencies(antlr_version, JAVA)

# Load //builder/grpc
load("@typedb_dependencies//builder/proto_grpc:deps.bzl", proto_grpc_deps = "deps")
proto_grpc_deps()
load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

# Load //tool/common
load("@typedb_dependencies//tool/common:deps.bzl", "typedb_dependencies_ci_pip",
    typedb_dependencies_tool_maven_artifacts = "maven_artifacts")
typedb_dependencies_ci_pip()

# Load //tool/checkstyle
load("@typedb_dependencies//tool/checkstyle:deps.bzl", checkstyle_deps = "deps")
checkstyle_deps()

# Load //tool/sonarcloud
load("@typedb_dependencies//tool/sonarcloud:deps.bzl", "sonarcloud_dependencies")
sonarcloud_dependencies()

# Load //tool/unuseddeps
load("@typedb_dependencies//tool/unuseddeps:deps.bzl", unuseddeps_deps = "deps")
unuseddeps_deps()

######################################
# Load @typedb_bazel_distribution #
######################################

load("@typedb_dependencies//distribution:deps.bzl", "typedb_bazel_distribution")
typedb_bazel_distribution()

# Load //common
load("@typedb_bazel_distribution//common:deps.bzl", "rules_pkg")
rules_pkg()
load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
rules_pkg_dependencies()

# Load //github
load("@typedb_bazel_distribution//github:deps.bzl", "ghr_osx_zip", "ghr_linux_tar")
ghr_osx_zip()
ghr_linux_tar()

# Load //maven
load("@typedb_bazel_distribution//maven:deps.bzl", typedb_bazel_distribution_maven_artifacts = "maven_artifacts")

# Load //pip
load("@typedb_bazel_distribution//pip:deps.bzl", "typedb_bazel_distribution_pip")
typedb_bazel_distribution_pip()


###############
# Load @maven #
###############
load("//dependencies/maven:artifacts.bzl", vaticle_force_layout_artifacts = "artifacts")

load("@typedb_dependencies//library/maven:rules.bzl", "maven")
maven(
    typedb_dependencies_tool_maven_artifacts +
    vaticle_force_layout_artifacts +
    typedb_bazel_distribution_maven_artifacts,
)

############################################
# Create @vaticle_force_layout_workspace_refs #
############################################
load("@typedb_bazel_distribution//common:rules.bzl", "workspace_refs")
workspace_refs(
    name = "vaticle_force_layout_workspace_refs"
)
