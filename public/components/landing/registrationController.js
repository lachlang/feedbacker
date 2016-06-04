/*
 * Controller for feedback action
 */
fbControllers.controller('RegistrationCtrl',  ['$scope', '$log', '$location', 'Account', function($scope, $log, $location, Account) {

	var ctrl = this;
	ctrl.error = undefined;
	ctrl.message = undefined;

	ctrl.register = function() {
		ctrl.message = undefined;
		ctrl.error = undefined;

		Account.register(ctrl.name, ctrl.role, ctrl.email, ctrl.password, ctrl.managerEmail).then(function(response) {
			ctrl.message = "Thankyou for registering.  An activation email has been sent to your email address."
//			$location.path("#/list");
//			$location.path("#/activation");
		}, function(response) {
			ctrl.error = "We could not register you at this time."
			$log.error("it failed")
			$log.error(response)
		});
	};

	$scope.$on('inactive-account', function() {
		$log.debug("Account is inactive");
		ctrl.error = "You account is not yet active.  Please follow the link sent in your activation email."
	});

}]);