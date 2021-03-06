const deserializer = require('./deserializer');

function sendRequestForTreesPreview() {
    const extension = common.getExtension(constants.fileName);
    if (extension === "ser" || extension === "fierix") {
        getAndShowTreesPreview();
    } else {
        common.showMessage(constants.pageMessages.callTreeUnavailable)
    }
}

$(window).on("load", function () {
    if (constants.fileName !== undefined) {
        common.doCallbackIfFileExists(
            constants.fileName,
            constants.projectName,
            sendRequestForTreesPreview,
            () => { // if does not exist
                common.redirect({project: constants.projectName})
            }
        );
    }
});

function getAndShowTreesPreview() {
    common.showLoader(constants.loaderMessages.buildingTrees, () => {
        const request = new XMLHttpRequest();
        const parameters = window.location.href.split("?")[1];
        request.open("GET", serverNames.CALL_TREE_PREVIEW_JS_REQUEST + "?" + parameters, true);
        request.responseType = "arraybuffer";

        request.onload = function () {
            common.hideLoader(0);
            if (request.status === 400) { // if file was not found
                common.showMessage(constants.pageMessages.chooseFile);
                return;
            }
            common.showLoader(constants.loaderMessages.deserialization, () => {
                const arrayBuffer = request.response;
                const byteArray = new Uint8Array(arrayBuffer);
                const treesPreview = deserializer.deserializeTreesPreview(byteArray);
                common.hideLoader(0);
                if (treesPreview.getTreesPreviewList().length !== 0) {
                    drawTreesPreview(treesPreview);
                } else {
                    showNoDataFound();
                }
            });
        };
        request.send();
    });
}

/**
 * @param {Object} treesPreview
 */
function drawTreesPreview(treesPreview) {
    common.showLoader(constants.loaderMessages.drawing, () => {
        const treesPreviewList = treesPreview.getTreesPreviewList();
        for (let i = 0; i < treesPreviewList.length; i++) {
            const drawer = new PreviewDrawer(
                treesPreviewList[i],
                i,
                treesPreview.getFullduration(),
                1, // TreePreviewProtos.TreePreview.Vector.VectorCase.X
                2  // TreePreviewProtos.TreePreview.Vector.VectorCase.Y
            );
            drawer.draw();
        }
        common.hideLoader(0);
    });
}
