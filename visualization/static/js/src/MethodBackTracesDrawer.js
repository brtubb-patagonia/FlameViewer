const BackTracesDrawer = require('./BackTracesDrawer');

module.exports.MethodBackTracesDrawer = class MethodBackTracesDrawer extends BackTracesDrawer.BackTracesDrawer {
    constructor(tree, className, methodName, desc, timePercent) {
        super(tree);
        this.class = className;
        this.method = methodName;
        this.desc = desc;
        this._setHeader(className, methodName, desc, timePercent);
    }

    _setHeader(className, methodName, desc, timePercent) {
        const $header = MethodBackTracesDrawer._createHeader(className, methodName, desc, timePercent);
        $header.insertBefore(this.$section.find(".canvas-wrapper"));
    }

    getTreeGETParameters(pathToNode) {
        return methodFunctions.getTreeGETParameters.call(this, pathToNode);
    }

    /**
     * @override
     * @param visibleLayersCount
     * @return {number}
     */
    _moveSectionUp(visibleLayersCount) {
        this.$fog.css("top", visibleLayersCount * (constants.LAYER_HEIGHT + constants.LAYER_GAP) + 59 + constants.METHOD_HEADER_HEIGHT);
        return 0;
    }
};