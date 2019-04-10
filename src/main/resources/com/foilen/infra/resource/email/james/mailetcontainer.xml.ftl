<?xml version="1.0"?>

<mailetcontainer enableJmx="true">

  <context>
    <postmaster>${postmasterEmail}</postmaster>
  </context>

  <spooler>
    <threads>20</threads>
  </spooler>

  <processors>

    <processor state="root" enableJmx="true">
    
      <mailet match="All" class="PostmasterAlias" />
      <mailet match="All" class="RecipientToLowerCase" />
      <mailet match="RelayLimit=30" class="Null" />

      <!-- Check if authenticated -->
      <mailet match="SMTPAuthSuccessful" class="SetMimeHeader">
        <name>X-UserIsAuth</name>
        <value>true</value>
      </mailet>

      <#if enableDebugDumpMessagesDetails >
        <mailet match="All" class="com.foilen.james.components.mailet.DumpAllSystemErr" />
      </#if>
      
      <!-- Check recipient's redirections -->
      <mailet match="All" class="com.foilen.james.components.mailet.ExactAndCatchAllRedirections">
        <cacheMaxTimeInSeconds>10</cacheMaxTimeInSeconds>
        <cacheMaxEntries>1000</cacheMaxEntries>
      </mailet>
      
      <!-- Local delivery -->
      <mailet match="RecipientIsLocal" class="ToProcessor">
        <processor>localProcessor</processor>
      </mailet>

      <!-- Local delivery - The domain is managed locally, but the local mailbox does not exist -->
      <mailet match="HostIsLocal" class="ToProcessor">
        <processor>localAccountDoesNotExistProcessor</processor>
        <notice>550 - Requested action not taken: no such user here</notice>
      </mailet>
      
      <!-- Outgoing -->
      <mailet match="com.foilen.james.components.matcher.HasHeaderGlobalAndSpecific=X-UserIsAuth" class="ToProcessor">
        <processor>outgoingAuthProcessor</processor>
      </mailet>
      
      <mailet match="All" class="ToProcessor">
        <processor>outgoingAnonymousProcessor</processor>
      </mailet>
      
    </processor>

    <processor state="localProcessor" enableJmx="true">
    
      <mailet match="All" class="com.foilen.james.components.mailet.LogInfo">
        <text>localProcessor</text>
      </mailet>
      
      <mailet match="All" class="AddDeliveredToHeader" />
      <mailet match="All" class="LocalDelivery" />
    
    </processor>
    
    <processor state="outgoingAuthProcessor" enableJmx="true">
    
      <mailet match="All" class="com.foilen.james.components.mailet.LogInfo">
        <text>outgoingAuthProcessor</text>
      </mailet>
      
      <!-- Relay emails per domain to different gateways -->
      <#list domainAndRelais as domainAndRelay>
      
        <mailet match="SenderIsRegex=(.*)@${domainAndRelay.a}" class="com.foilen.james.components.mailet.LogInfo">
          <text>Remote delivery via the gateway for SenderIsRegex=(.*)@${domainAndRelay.a}</text>
        </mailet>
        <mailet match="SenderIsRegex=(.*)@${domainAndRelay.a}" class="RemoteDelivery">
          <outgoing>outgoing-${domainAndRelay.a}</outgoing>
    
          <delayTime>5000, 100000, 500000</delayTime>
          <maxRetries>25</maxRetries>
          <maxDnsProblemRetries>0</maxDnsProblemRetries>
          <deliveryThreads>10</deliveryThreads>
          <sendpartial>true</sendpartial>
          <bounceProcessor>bounceProcessor</bounceProcessor>
          <gateway>${domainAndRelay.b.hostname}</gateway>
          <gatewayPort>${domainAndRelay.b.port}</gatewayPort>
          <gatewayUsername>${domainAndRelay.b.username}</gatewayUsername>
          <gatewayPassword>${domainAndRelay.b.password}</gatewayPassword>
        </mailet>
      </#list>
      
      <mailet match="All" class="ToProcessor">
        <processor>outgoingDirectRelayProcessor</processor>
      </mailet>
      
    </processor>

    <processor state="outgoingDirectRelayProcessor" enableJmx="true">
    
      <!-- Relay -->
      <mailet match="All" class="com.foilen.james.components.mailet.LogInfo">
        <text>Remote delivery via the server directly (no gateway)</text>
      </mailet>
      <mailet match="All" class="RemoteDelivery">
        <outgoing>outgoing</outgoing>

        <delayTime>5000, 100000, 500000</delayTime>
        <maxRetries>25</maxRetries>
        <maxDnsProblemRetries>0</maxDnsProblemRetries>
        <deliveryThreads>10</deliveryThreads>
        <sendpartial>true</sendpartial>
        <bounceProcessor>bounceProcessor</bounceProcessor>
      </mailet>
      
    </processor>
    
    <processor state="outgoingAnonymousProcessor" enableJmx="true">
    
      <mailet match="All" class="com.foilen.james.components.mailet.LogInfo">
        <text>outgoingAnonymousProcessor</text>
      </mailet>
      
      <!-- If is a redirection -> Send directly (since could be spam) -->
      <mailet match="com.foilen.james.components.matcher.HasHeaderGlobalAndSpecific=isRedirection" class="ToProcessor">
        <processor>outgoingDirectRelayProcessor</processor>
      </mailet>
      
      <!-- If is not a redirection -> Deny -->
      <mailet match="All" class="ToProcessor">
        <processor>outgoingDeniedProcessor</processor>
        <notice>550 - Requested action not taken: relaying denied</notice>
      </mailet>
      
    </processor>
    
    <processor state="localAccountDoesNotExistProcessor" enableJmx="true">
    
      <mailet match="All" class="com.foilen.james.components.mailet.LogInfo">
        <text>localAccountDoesNotExistProcessor</text>
      </mailet>
    
      <mailet match="All" class="Bounce">
        <attachment>none</attachment>
      </mailet>
      
    </processor>

    <processor state="outgoingDeniedProcessor" enableJmx="true">
    
      <#if !disableRelayDeniedNotifyPostmaster >
        <mailet match="All" class="NotifyPostmaster">
          <sender>unaltered</sender>
          <attachError>true</attachError>
          <prefix>[RELAY-DENIED]</prefix>
          <passThrough>true</passThrough>
          <to>postmaster</to>
          <debug>true</debug>
        </mailet>
      </#if>

      <mailet match="All" class="Null" />
      
    </processor>

    <processor state="bounceProcessor" enableJmx="true">
    
      <#if !disableBounceNotifyPostmaster >
        <mailet match="All" class="NotifyPostmaster">
          <sender>unaltered</sender>
          <attachError>true</attachError>
          <prefix>[BOUNCE]</prefix>
          <passThrough>true</passThrough>
          <to>postmaster</to>
          <debug>true</debug>
        </mailet>
      </#if>
      
      <#if !disableBounceNotifySender >
        <mailet match="All" class="DSNBounce">
          <passThrough>true</passThrough>
        </mailet>
      </#if>
      
      <mailet match="All" class="Null" />
      
    </processor>
    
    <processor state="error" enableJmx="true">
    
      <mailet match="All" class="NotifyPostmaster">
        <sender>unaltered</sender>
        <attachError>true</attachError>
        <prefix>[ERROR]</prefix>
        <passThrough>false</passThrough>
        <to>postmaster</to>
        <debug>true</debug>
      </mailet>
      
    </processor>
    
  </processors>

</mailetcontainer>
