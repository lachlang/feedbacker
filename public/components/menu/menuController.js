/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
fbControllers.controller('MenuCtrl', ['$rootScope', '$log', '$location', 'Session', function($rootScope, $log, $location, Session) {

	var ctrl = this;

	ctrl.error = undefined;
	ctrl.isLeader = false;

	ctrl.login = function() {
		ctrl.resetError();

		// LG: 2016-04-26 use standard form validation only at this point
		Session.login(ctrl.username, ctrl.password).then(function(result) {
			$log.debug("Logged in...");
			$location.path("/list");
		}, function(response) {
			$log.error("Login FAILED!");
			if (response.status == 401) {
				$rootScope.$broadcast('inactive-account', {});
			} else if (response.status == 400) {
				$rootScope.$broadcast('invalid-credentials', {});
			}
			ctrl.error = "Could not log in.  Please try again later.";
		});
		ctrl.password = undefined;
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
		return Session.validSession();
	}

	ctrl.isLoggedInLeader = function() {
		return Session.validSession() && Session.isLeader();
	}

	ctrl.isActive = function (viewLocation) {
		return viewLocation === $location.path();
	};

}]);