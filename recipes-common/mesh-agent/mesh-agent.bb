
SUMMARY = "Mesh Agent"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"


DEPENDS = "ccsp-common-library utopia dbus rdk-logger telemetry wrp-c cjson libparodus"
DEPENDS_append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"

DEPENDS_append_dunfell = " trower-base64"
RDEPENDS_${PN}_append_dunfell = " bash"

LDFLAGS_remove_dunfell = "-lsafec-3.5"
LDFLAGS_append_dunfell = " -lsyscfg -lsysevent -lbreakpadwrapper -lsafec-3.5.1"

require recipes-ccsp/ccsp/ccsp_common.inc

SRC_URI = "${CMF_GIT_ROOT}/rdkb/components/opensource/ccsp/MeshAgent;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH};name=mesh-agent"

SRCREV_mesh-agent = "${AUTOREV}"
PV = "${RDK_RELEASE}+git${SRCPV}"

CFLAGS_append = " \
    -I${STAGING_INCDIR}/dbus-1.0 \
    -I${STAGING_LIBDIR}/dbus-1.0/include \
    -I${STAGING_INCDIR}/ccsp \
    -I${STAGING_INCDIR}/trower-base64 \
    -I${STAGING_INCDIR}/libsafec \
    -I${STAGING_INCDIR}/cjson \
    -I${STAGING_INCDIR}/wrp-c \
    -I${STAGING_INCDIR}/libparodus \
    -DENABLE_MESH_SOCKETS \
    "
CFLAGS_append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"
CFLAGS_append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"

CFLAGS += " -Wall -Werror -Wextra -Wno-pointer-sign -Wno-int-to-pointer-cast -Wno-address "

LDFLAGS_append = " \
    -ldbus-1 \
    -lrdkloggers \
    -ltelemetry_msgsender \
    -lm \
    -lcjson \
    -lwrp-c \
    -llibparodus \
"
LDFLAGS_append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"

S = "${WORKDIR}/git"

inherit autotools systemd pkgconfig

EXTRA_OECONF_append = " ${@bb.utils.contains('DISTRO_FEATURES', 'gtestapp', '--enable-gtestapp', '', d)}"

do_install_append () {
    # Config files and scripts
    install -d ${D}/usr/ccsp/mesh
    install -d ${D}/usr/include/mesh
    install -m 644 ${S}/source/include/*.h ${D}/usr/include/mesh
    install -m 644 ${S}/config/TR181-MeshAgent.xml -t ${D}/usr/ccsp/mesh
    install -m 755 ${S}/scripts/active_host_filter.sh -t ${D}/usr/ccsp/mesh
    ln -sf /usr/bin/meshAgent ${D}/usr/ccsp/mesh/meshAgent

    install -d ${D}${systemd_unitdir}/system

    #CCSP Initialization Targets
    install -D -m 0644 ${S}/systemd_units/meshAgent.path ${D}${systemd_unitdir}/system/meshAgent.path
    install -D -m 0644 ${S}/systemd_units/meshwifi.service ${D}${systemd_unitdir}/system/meshwifi.service


    install -m 775 ${S}/config/MeshAgent.cfg -t ${D}/usr/ccsp/mesh
    install -m 775 ${S}/config/MeshAgentDM.cfg -t ${D}/usr/ccsp/mesh
}

PACKAGES += "${PN}-ccsp"

# IMPORTANT! Do not add meshwifi.service to the SYSTEMD_SERVICE define below. We don't want it automatically
# started.
SYSTEMD_SERVICE_${PN} = " ${@bb.utils.contains('DISTRO_FEATURES', 'meshwifi', 'meshAgent.path', '', d)}"

FILES_${PN}-ccsp = " \
    /fss/gw/usr/ccsp/* \
    ${prefix}/ccsp/mesh/* \
    ${prefix}/ccsp/* \
"

FILES_${PN} = " \
    /usr/bin/meshAgent \
    ${prefix}/ccsp/mesh/meshAgent \
    ${prefix}/ccsp/mesh/MeshAgent.cfg \
    ${prefix}/ccsp/mesh/MeshAgentDM.cfg \
    ${prefix}/ccsp/mesh/TR181-MeshAgent.xml \
    ${prefix}/ccsp/mesh/active_host_filter.sh \
    ${systemd_unitdir}/system/meshAgent.path \
    ${systemd_unitdir}/system/meshwifi.service \
    ${libdir}/libMeshAgentSsp.so* \
"

FILES_${PN}-dbg = " \
    ${prefix}/ccsp/mesh/.debug \
    ${prefix}/src/debug \
    ${bindir}/.debug \
    ${libdir}/.debug \
"

