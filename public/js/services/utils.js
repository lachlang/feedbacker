fbServices.service('Utils', ['$log', function($log) {

	var validSession = false;

	return {
		getLastTokenFromUrl: function(url) {
			if (url == undefined) {
				return
			}
			var urlTokens = url.split('/');
			if (!urlTokens.isArray() || urlTokens.length < 1) {
				return
			}
			return urlTokens[urlTokens.length - 1];
		}
	}
}]);