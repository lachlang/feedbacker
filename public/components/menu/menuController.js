/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
fbControllers.controller('MenuCtrl', ['$rootScope', '$log', '$location', 'Session', 'Model', function($rootScope, $log, $location, Session, Model) {

	var ctrl = this;

	ctrl.error = undefined;
	ctrl.isLeader = false;
	ctrl.isAdmin = false;

	ctrl.login = function() {
		ctrl.resetError();

		// LG: 2016-04-26 use standard form validation only at this point
		Session.login(ctrl.username, ctrl.password).then(function(result) {
			$log.debug("Logged in...");
			ctrl.initialiseAuthenticatedContent();
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
		ctrl.isLeader = false;
		ctrl.isAdmin = true;
		Model.flush();
		Session.logout();
		$location.path("/landing");
	};

    ctrl.initialiseAuthenticatedContent = function() {
        Model.getCurrentUser().then(function(result) {
            ctrl.isLeader = result.isLeader;
            ctrl.isAdmin = !!result.isAdmin;
            $log.debug("[MenuCtrl.initialiseAuthenticatedContent] Updated. { isLeader: " + ctrl.isLeader + ", isAdmin: " + ctrl.isAdmin + "}");
        }, function() {
            // do nothing on error, cache is not initialised
        });
    };

	ctrl.resetError = function() {
		ctrl.error = undefined;
	};

	ctrl.isLoggedIn = function() {
		return Session.validSession();
	}

	ctrl.isLoggedInLeader = function() {
		return Session.validSession() && ctrl.isLeader;
	}

	ctrl.isLoggedInAdmin = function() {
		return Session.validSession() && ctrl.isAdmin;
	}

	ctrl.isActive = function (viewLocation) {
		return viewLocation === $location.path();
	};

    /**
     *  Trigger cache load on page refresh if valid session cookie exists.
     */
     if (ctrl.isLoggedIn()) {
        $log.debug("[MenuCtrl] Initialising session after page refresh...");
        ctrl.initialiseAuthenticatedContent();
     } else {
        $log.debug("[MenuCtrl] Session not authenticated after page load.")
     }
}]);