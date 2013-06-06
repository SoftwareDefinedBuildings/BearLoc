from twisted.application import internet, service
from twisted.web import resource as twisted_resource, server

from boss.service import BOSSService
from boss import resource as boss_resource


application = service.Application('boss')
boss = BOSSService()
serviceCollection = service.IServiceCollection(application)
# HTTP service
site = server.Site(twisted_resource.IResource(boss))
internet.TCPServer(10080, site).setServiceParent(serviceCollection)