DESCRIPTION = "Microsoft Azure IoT SDKs and libraries for C"
AUTHOR = "Microsoft Corporation"
HOMEPAGE = "https://github.com/Azure/azure-iot-sdk-c"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4283671594edec4c13aeb073c219237a"

inherit cmake python-dir

DEPENDS = "\
	azure-c-shared-utility \
	azure-uamqp-c \
	azure-umqtt-c \
"

SRC_URI = "\
	gitsm://github.com/Azure/azure-iot-sdk-python.git \
	file://0001-Refactor-cmake-if-statements.patch \
	file://0002-Only-run-tests-if-requested.patch \
"
SRCREV = "5af0e09e09ae2ba41c1e6b70efa2f1d431a61a26"

PR = "r0"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

# List of packages to build
PACKAGES = "\
	${PN}-c \
	${PN}-c-dev \
	python-${PN} \
	${PN}-dbg \
"

PACKAGECONFIG ??= "python"
PACKAGECONFIG[python] = "-Dbuild_python:STRING=${PYTHON_BASEVERSION}, -Dbuild_python:BOOL=OFF, ${PYTHON_PN} boost, boost-python azure-iot-sdk-c"

do_configure_prepend() {
	cd ${S}
	git config --global http.sslverify "false"
	git submodule update --init --recursive
}

## CMake ##
OECMAKE_SOURCEPATH = "${S}/c"
EXTRA_OECMAKE = "-DBUILD_SHARED_LIBS:BOOL=ON -Dskip_samples:BOOL=ON -Dskip_unittests:BOOL=ON -Duse_installed_dependencies:BOOL=ON"


do_install_append() {
	# Python
	if ${@bb.utils.contains('PACKAGECONFIG','python','true','false',d)}; then
		install -d ${D}${PYTHON_SITEPACKAGES_DIR}
		oe_libinstall -C ${B}/python/src -so iothub_client ${D}${PYTHON_SITEPACKAGES_DIR}
		oe_libinstall -C ${B}/python_service_client/src -so iothub_service_client ${D}${PYTHON_SITEPACKAGES_DIR}
	fi
}

sysroot_stage_all_append () {
	sysroot_stage_dir ${D}${exec_prefix}/cmake ${SYSROOT_DESTDIR}${exec_prefix}/cmake

	# Fix CMake configs
	sed -i 's#${libdir}#${STAGING_LIBDIR}#g' ${SYSROOT_DESTDIR}${exec_prefix}/cmake/azure_iot_sdksTargets*
	sed -i 's#${includedir}/azureiot#${STAGING_INCDIR}/azureiot#g' ${SYSROOT_DESTDIR}${exec_prefix}/cmake/azure_iot_sdksTargets*
}

FILES_${PN}-c = "${libdir}/*.so"

RDEPENDS_${PN}-c-dev = "\
	azure-iot-sdk-c \
	azure-umqtt-c-dev \
	azure-uamqp-c-dev \
	azure-c-shared-utility-dev \
"
FILES_${PN}-c-dev += "\
	${includedir} \
	${exec_prefix}/cmake \
"

FILES_python-${PN} += "\
	${PYTHON_SITEPACKAGES_DIR}/*.so \
"

FILES_${PN}-dbg += "\
	${libdir}/.debug \
	${PYTHON_SITEPACKAGES_DIR}/.debug \
"

RRECOMMENDS_azure-iot-sdk-c-dev[nodeprrecs] = "1"

INSANE_SKIP_python-${PN} += "rpaths"
