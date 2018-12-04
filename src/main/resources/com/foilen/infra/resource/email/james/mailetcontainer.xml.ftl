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

      <mailet match="HasMailAttribute=spamChecked" class="ToProcessor">
        <processor>transport</processor>
      </mailet>

      <mailet match="All" class="SetMailAttribute">
        <spamChecked>true</spamChecked>
      </mailet>

      <mailet match="All" class="ToProcessor">
        <processor>transport</processor>
      </mailet>

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


    <processor state="transport" enableJmx="true">
    
      <mailet match="All" class="com.foilen.james.components.mailet.LogInfo">
        <text>Is in transport</text>
      </mailet>
      
      <#if enableDebugDumpMessagesDetails >
        <mailet match="All" class="com.foilen.james.components.mailet.DumpAllSystemErr" />
      </#if>
    
      <!-- Add some headers -->
      <mailet match="SMTPAuthSuccessful" class="SetMimeHeader">
        <name>X-UserIsAuth</name>
        <value>true</value>
      </mailet>
      <mailet match="HasMailAttribute=X-UserIsAuth" class="com.foilen.james.components.mailet.LogInfo">
        <text>User is SMTPAuthSuccessful</text>
      </mailet>

      <mailet match="HasMailAttribute=org.apache.james.SMIMECheckSignature" class="SetMimeHeader">
        <name>X-WasSigned</name>
        <value>true</value>
      </mailet>
      
      <!-- Check recipient's redirections -->
      <mailet match="All" class="com.foilen.james.components.mailet.ExactAndCatchAllRedirections">
      	<cacheMaxTimeInSeconds>10</cacheMaxTimeInSeconds>
      	<cacheMaxEntries>1000</cacheMaxEntries>
      </mailet>
      <mailet match="HasMailAttribute=isRedirection" class="com.foilen.james.components.mailet.LogInfo">
        <text>Recipient is ExactAndCatchAllRedirections</text>
      </mailet>

      <!-- Local delivery -->
      <mailet match="RecipientIsLocal" class="com.foilen.james.components.mailet.LogInfo">
        <text>Recipient is Local</text>
      </mailet>
      <mailet match="RecipientIsLocal" class="AddDeliveredToHeader" />
      <mailet match="RecipientIsLocal" class="LocalDelivery" />

      <!-- Local delivery - The domain is managed locally, but the local mailbox does not exist -->
      <mailet match="HostIsLocal" class="com.foilen.james.components.mailet.LogInfo">
        <text>Recipient host is local, but the recipient is not found</text>
      </mailet>
      <mailet match="HostIsLocal" class="ToProcessor">
        <processor>local-address-error</processor>
        <notice>550 - Requested action not taken: no such user here</notice>
      </mailet>
      
      <!-- Remote delivery when destination was changed by ExactAndCatchAllRedirections -->
      <mailet match="HasMailAttribute=isRedirection" class="com.foilen.james.components.mailet.LogInfo">
        <text>Remote delivery when destination was changed by ExactAndCatchAllRedirections</text>
      </mailet>
      <mailet match="HasMailAttribute=isRedirection" class="ToProcessor">
        <processor>auth-user-relay</processor>
      </mailet>

      <!-- Is a user and needs to relay his emails -->
      <mailet match="SentByMailet" class="com.foilen.james.components.mailet.LogInfo">
        <text>Relay since SentByMailet</text>
      </mailet>
      <mailet match="SentByMailet" class="ToProcessor">
      	<processor>auth-user-relay</processor>
      </mailet>
      <mailet match="com.foilen.james.components.matcher.SenderIsLocalAndSameAsSMTPAuth" class="com.foilen.james.components.mailet.LogInfo">
        <text>Relay since SenderIsLocalAndSameAsSMTPAuth</text>
      </mailet>
      <mailet match="com.foilen.james.components.matcher.SenderIsLocalAndSameAsSMTPAuth" class="ToProcessor">
      	<processor>auth-user-relay</processor>
      </mailet>

      <!-- Not an open relay -->
      <mailet match="All" class="com.foilen.james.components.mailet.LogInfo">
        <text>Not an open relay</text>
      </mailet>
      <mailet match="All" class="ToProcessor">
        <processor>relay-denied</processor>
        <notice>550 - Requested action not taken: relaying denied</notice>
      </mailet>

    </processor>


    <processor state="auth-user-relay" enableJmx="true">
    
      <#if enableDebugDumpMessagesDetails >
        <mailet match="All" class="com.foilen.james.components.mailet.DumpAllSystemErr" />
      </#if>
    
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
          <bounceProcessor>bounces</bounceProcessor>
          <gateway>${domainAndRelay.b.hostname}</gateway>
          <gatewayPort>${domainAndRelay.b.port}</gatewayPort>
          <gatewayUsername>${domainAndRelay.b.username}</gatewayUsername>
          <gatewayPassword>${domainAndRelay.b.password}</gatewayPassword>
        </mailet>
      </#list>
      
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
        <bounceProcessor>bounces</bounceProcessor>
      </mailet>

    </processor>

    
    <processor state="spam" enableJmx="true">
    
      <mailet match="RecipientIsLocal" class="ToRecipientFolder">
        <folder>SPAM</folder>
        <consume>true</consume>
      </mailet>
      
    </processor>


    <processor state="virus" enableJmx="true">
    
      <mailet match="All" class="NotifyPostmaster">
        <sender>unaltered</sender>
        <attachError>true</attachError>
        <prefix>[VIRUS]</prefix>
        <passThrough>true</passThrough>
        <to>postmaster</to>
        <debug>true</debug>
      </mailet>
    
      <mailet match="All" class="SetMailAttribute">
        <org.apache.james.infected>true, bouncing</org.apache.james.infected>
      </mailet>

      <mailet match="SMTPAuthSuccessful" class="Bounce">
        <inline>heads</inline>
        <attachment>none</attachment>
        <notice>Warning: We were unable to deliver the message below because it was found infected by virus(es).</notice>
      </mailet>

      <mailet match="All" class="Null" />
      
    </processor>


    <processor state="local-address-error" enableJmx="true">
    
      <mailet match="All" class="NotifyPostmaster">
        <sender>unaltered</sender>
        <attachError>true</attachError>
        <prefix>[LOCAL ADDRESS ERROR]</prefix>
        <passThrough>true</passThrough>
        <to>postmaster</to>
        <debug>true</debug>
      </mailet>
      
      <mailet match="All" class="Bounce">
        <attachment>none</attachment>
      </mailet>
      
    </processor>


    <processor state="relay-denied" enableJmx="true">
    
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


    <processor state="bounces" enableJmx="true">
    
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

  </processors>

</mailetcontainer>
