fbServices.service('Nomination', ['$log', '$http', function($log, $http) {

	return {

		getCurrentNominations: function() {
			return $http.get("/api/nominations");
		},

		addNomination: function(username, cycleId, message) {
			return $http({
				method: "POST",
				url: "/api/nominations",
				data:{
					apiVersion: "1.0",
					body: {
						username: username,
						cycleId: cycleId,
						message: message
					}
				}
			})
		},

		cancelNomination: function(id) {
			// LG it would be better to use DELETE here but I'm not sure
			// if it blocked by the firewall
			return $http.get("/api/nominations/cancel/" + id);
		}
	}
}]);