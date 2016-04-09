/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
brewTrackerApp.controller('RegistrationCtrl', ['$scope', '$log', 'Registration', '$location', 'Errors',
function($scope, $log, Registration, $location, Errors) {

	var ctrl = this;

	ctrl.register = function(isValid) {
		ctrl.registered = undefined;

		// TODO: validation

		Registration.register(ctrl.firstName, ctrl.lastName, ctrl.emailAddress, ctrl.password).then(
			function(result) {
				// success
				$log.debug("[RegistrationCtrl.register] Success: [" + result.status + "]");
				$log.debug(result.data);
				if(result.status == 201 || result.status == 266) {

					ctrl.registered = "Thank you " + ctrl.firstName + ".  You have successfully registered."
				} else {
					ctrl.registered = "This is not the success you are looking for.";
				}
			}, function(result) {
				// error
				$log.debug("[RegistrationCtrl.register] Failure: [" + result.status + "]");
				$log.debug(result.data);
				if (result.data.error && result.data.error.message == Errors.REGISTER_EMAIL_DUP_CODE) {
					ctrl.registered = Errors.REGISTER_EMAIL_DUP_MESSAGE;
				} else {
					ctrl.registered = Errors.REGISTER_FAILED_MESSAGE;
				}
			});
	};

	ctrl.test = function() {
		Registration.register("Lachlan", "Gartside", "lachlang@gmail.com", "pants").then(
			function(result) {
				$log.debug("[RegistrationCtrl.test] Success: [" + result.status + "]");
				$log.debug(result.data);
				ctrl.testResponse = result.data
			}, function(result) {
				$log.debug("[RegistrationCtrl.test] Error: [" + result.status + "]");
				$log.debug(result.data);
				ctrl.testResponse = result.data

			});
	}
}]);