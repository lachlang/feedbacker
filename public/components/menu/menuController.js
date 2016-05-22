/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
fbControllers.controller('MenuCtrl', ['$scope', '$log', '$location', 'Session', function($scope, $log, $location, Session) {

	var ctrl = this;

	ctrl.error = undefined;

	ctrl.login = function() {
		ctrl.resetError();

		// LG: 2016-04-26 use standard form validation only at this point
		Session.login(ctrl.username, ctrl.password).then(function(result) {
			$location.path("/list");
		}, function(result) {
			$log.error("Login FAILED!")
			ctrl.error = "Could not log in.  Please try again later.";
		});
		ctrl.password = undefined;
		$log.debug("this is a test function which will print text to the browser console...");
	};

	ctrl.logout = function() {
		ctrl.resetError();
		Session.logout();
		$location.path("/landing");
	};

	ctrl.resetError = function() {
		ctrl.error = undefined;
	}

	ctrl.isLoggedIn = function() {
		return Session.isLoggedIn();
	}
}]);