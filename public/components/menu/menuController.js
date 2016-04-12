/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
fbControllers.controller('LoginCtrl', ['$scope', '$log', function($scope, $log) {

	var ctrl = this;

	ctrl.testy = function() {
		$log.debug("this is a test function which will print text to the browser console...");
	};
}]);