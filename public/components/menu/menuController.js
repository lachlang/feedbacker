/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
fbControllers.controller('MenuCtrl', ['$scope', '$log', '$location', 'Session', function($scope, $log, $location, Session) {

	var ctrl = this;

	var isLoggedIn = false;
	ctrl.error = undefined;

	ctrl.login = function() {
		ctrl.resetError();

		// LG: 2016-04-26 use standard form validation only at this point
		Session.login().then(function(result) {
			hasLoggedIn = true;
			$location.path("/list");
			$log.info("logged in!")
		}, function(result) {
			$log.error("log in FAILED!")
			ctrl.error = "Could not log in.  Please try again later.";
		});
		$log.debug("this is a test function which will print text to the browser console...");
	};

	ctrl.logout = function() {
		ctrl.resetError();
		$location.path("/landing");
	};

	ctrl.resetError = function() {
		ctrl.error = undefined;
	}

	ctrl.hasLoggedIn = function() {
		return isLoggedIn;
	}
}]);